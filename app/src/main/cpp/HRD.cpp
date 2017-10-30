#include "HRD.h"

Mat frames[MAX_LENGTH];
float bvp[MAX_LENGTH][ROI_ROWS][ROI_COLS];
float perFrame_BVP[MAX_LENGTH];

/*
** @brief - Constructor of the class HeartRateDetect Object.
** @parameter - void
*/
HRD::HRD(){
	
	levels = 4;
	fl = 2.0f;
	fh = 2.6f;
	fps = 25.0f;
	heartRate = 0.0f;
	LENGTH = MAX_LENGTH;

	for (size_t i = 0; i < LENGTH; i++)
	{
		perFrame_BVP[i] = 0;
	}
}

HRD::~HRD(){

}

bool HRD::videoGet(const char*fileName){
	bool inputIsOK = true;
	VideoCapture capture(fileName);
	if (!capture.isOpened()){
		inputIsOK = false;
		return inputIsOK;
	}
	int sumOfFrames = LENGTH;
	int index(0);
	do{
		capture >> frames[index];
		index++;
	} while (index < LENGTH);
	return inputIsOK;
}

/*
**@brief - Gaussian pyramid image decomposition
**@parameter - void
*/
void HRD::gaussPyramid(){
	int index(0);
	do
	{
		// Cut out the XT profile first
		Mat gaussFrame = frames[index];
		for (int i = 0; i < levels; i++){
			GaussianBlur(gaussFrame, gaussFrame, Size(5, 5), 0, 0, BORDER_DEFAULT);
			pyrDown(gaussFrame, gaussFrame, Size(gaussFrame.cols / 2, gaussFrame.rows / 2));
		}
		// The conversion of color gamut decomposition by the top pyramid
		Mat YIQ_tmp(gaussFrame.size(), CV_32FC3);
		YIQ_tmp = Rgb2Ntsc(gaussFrame);

		frames[index] = YIQ_tmp.clone();
		index++;
	} while (frames[index].data&&index < LENGTH);
}

/*
**@brief - Ideal band-pass filter, get the heart rate signal.
**@parameter - void
*/
void HRD::idealBandPass(){
	float* Freq = new float[LENGTH];

	for (int i = 0; i < LENGTH; i++){
		Freq[i] = ((float)i) / LENGTH * fps;
	}

	float *mask_array = new float[LENGTH];
	for (int i = 0; i < LENGTH; i++){
		if (Freq[i]<fh&&Freq[i]>fl){
			mask_array[i] = 1.0f;
		}
		else{
			mask_array[i] = 0.0f;
		}
	}
	Mat mask(1, LENGTH, CV_32F, mask_array);

	int rowsNum = frames[0].rows;
	int colsNum = frames[0].cols;

	float*perPixel = new float[LENGTH];

	//I, j pixel control position , k control frames
	for (int i = 0; i < rowsNum; i++)
	{
		for (int j = 0; j < colsNum; j++)
		{
			for (int k = 0; k < LENGTH; k++){
				perPixel[k] = frames[k].at<Vec3f>(i, j)[0];
			}
			Mat temp(1, LENGTH, CV_32F, perPixel);
			dft(temp, temp);
			mulSpectrums(temp, mask, temp, DFT_ROWS);
			idft(temp, temp);
			for (int k = 0; k < LENGTH; k++){
				bvp[k][i][j] = temp.at<float>(k);
			}
		}
	}
	delete[] Freq;
	delete[] mask_array;
	delete[] perPixel;
}

/*
**@brief - Compute the heartRate by perFrame_bvp.
**@parameter - void
*/
void HRD::caculateHRValue(){
	float sum(0.0f);
	float avg_sum(0.0f);

	int rowsNum = frames[0].rows;
	int colsNum = frames[0].cols;

	for (int k = 0; k < LENGTH; k++){
		sum = 0.0f;
		for (int i = 0; i < rowsNum; i++){
			for (int j = 0; j < colsNum; j++){
				sum += bvp[k][i][j];
			}
		}
		avg_sum = sum / (rowsNum*colsNum);
		perFrame_BVP[k] = avg_sum;
	}

	heartRate = IFE_Detecting(perFrame_BVP, fps, 500, LENGTH);
}

float HRD::getHRValue(){
	return heartRate;
}

Mat HRD::Rgb2Ntsc(Mat&frame)
{
	Mat dst = frame.clone();
	frame.convertTo(frame, CV_32FC3);
	dst.convertTo(dst, CV_32FC3);

	for (int i = 0; i < frame.rows; i++)
	{
		for (int j = 0; j < frame.cols; j++){
			dst.at<Vec3f>(i, j)[2] = saturate_cast<float>(((0.299*frame.at<Vec3f>(i, j)[2] +
				0.587*frame.at<Vec3f>(i, j)[1] +
				0.114*frame.at<Vec3f>(i, j)[0])) / 255);
			dst.at<Vec3f>(i, j)[1] = saturate_cast<float>(((0.596*frame.at<Vec3f>(i, j)[2] +
				-0.274*frame.at<Vec3f>(i, j)[1] +
				-0.322*frame.at<Vec3f>(i, j)[0])) / 255);
			dst.at<Vec3f>(i, j)[0] = saturate_cast<float>(((0.211*frame.at<Vec3f>(i, j)[2] +
				-0.523*frame.at<Vec3f>(i, j)[1] +
				0.312*frame.at<Vec3f>(i, j)[0])) / 255) * 200;
		}
	}
	return dst;
}

float HRD::IFE_Detecting(float*bvp, float fs, int Q, int LENGTH)
{
	int N = LENGTH;
	bool show_flag = false;

	Mat tmp_X(1, N, CV_32F, bvp);
	Mat X = tmp_X.clone();
	X.mul(100);
	
	Mat planes[] = { Mat_<float>(X), Mat::zeros(X.size(), CV_32F) };
	Mat complexI;

	merge(planes, 2, complexI);
	dft(complexI, complexI);

	split(complexI, planes);
	magnitude(planes[0], planes[1], planes[0]);

	//
	Mat shift_planes = fftshift(planes[0], planes[0].cols);
	
	//
	Mat k(1, N, CV_32F);
	//
	Mat freq_All(1, N, CV_32F);

	int fN = N - N % 2;
	float temp = -fN / 2;

	for (int i = 0; i < LENGTH; i++)
	{
		k.at<float>(0, i) = temp++;
	}

	float T = N / fs;
	float detaF = 1 / T;

	freq_All = k.mul(detaF);
	int one_IDX = fN / 2 + 1;

	Mat freq_Positive = freq_All(Rect(one_IDX, 0, N - one_IDX, 1)).clone();
	Mat amp = shift_planes(Rect(one_IDX, 0, N - one_IDX, 1)).clone();
	

	float*pows = new float[LENGTH];
	for (int i = 0; i < amp.cols; i++){
		pows[i] = pow(amp.at<float>(i), 2);
	}
	
	int max_pos = max3(pows, amp.cols);
	float dQ = detaQ(bvp, max_pos, LENGTH, 500);

	delete[] pows;
	return abs((freq_Positive.at<float>(max_pos)) + dQ*detaF)*60.0f;
}

float HRD::detaQ(float*bvp, float max_IDX, int N, int Q)
{
	bool show_flag = false;
	max_IDX++;

	float deta = 0.0f;
	float p1 = -0.5f;
	float p2 = 0.5f;

	float*e1_Imaginary = new float[N];
	float*e1_Real = new float[N];

	float*e2_Imaginary = new float[N];
	float*e2_Real = new float[N];

	float H = 0.0f;;
	float x1_Real = 0.0f;
	float x1_Imaginary = 0.0f;

	float x2_Real = 0.0f;
	float x2_Imaginary = 0.0f;

	for (int j = 0; j < Q; j++)
	{
		x1_Real = 0.0f;
		x2_Real = 0.0f;
		x1_Imaginary = 0.0f;
		x2_Imaginary = 0.0f;

		float test1 = 0.0f;
		float test2 = 0.0f;

		for (int i = 0; i < N; i++)
		{
			x1_Real += cos((-2 * PI*(max_IDX + deta + p1) / N)*i)*bvp[i];
			x1_Imaginary += sin((-2 * PI*(max_IDX + deta + p1) / N)*i)*bvp[i];
			

			x2_Real += cos((-2 * PI*(max_IDX + deta + p2) / N)*i)*bvp[i];
			x2_Imaginary += sin((-2 * PI*(max_IDX + deta + p2) / N)*i)*bvp[i];
		}

		H = 0.5*(pow((pow(x2_Real, 2) + pow(x2_Imaginary, 2)), 0.5) - pow((pow(x1_Real, 2) + pow(x1_Imaginary, 2)), 0.5)) /
			(pow((pow(x2_Real, 2) + pow(x2_Imaginary, 2)), 0.5) + pow((pow(x1_Real, 2) + pow(x1_Imaginary, 2)), 0.5));
		deta += H;
		
	}
	delete[] e1_Imaginary;
	delete[] e1_Real;
	delete[] e2_Imaginary;
	delete[] e2_Real;

	return deta;
}

Mat HRD::fftshift(Mat&X, int length)
{
	Mat dst(X.size(), CV_32F);
	if (length % 2){
		int i;
		for (i = 0; i < length / 2; i++)
		{
			dst.at<float>(i) = X.at<float>(length / 2 + i + 1);
			dst.at<float>(length / 2 + i) = X.at<float>(i);
		}
		dst.at<float>(length - 1) = X.at<float>(length / 2);
	}
	else{
		for (int i = 0; i < length / 2; i++)
		{
			dst.at<float>(i) = X.at<float>(length / 2 + i);
			dst.at<float>(length / 2 + i) = X.at<float>(i);
		}
	}
	return dst;
}

int HRD::max3(float*pows, int length)
{
	int flag(0);
	int temp(pows[0]);
	for (int i = 0; i < length; i++){
		if (temp < pows[i]){
			flag = i;
			temp = pows[i];
		}
	}
	return flag;
}