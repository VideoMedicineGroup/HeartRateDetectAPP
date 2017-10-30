/*
**	Brief£ºNon-contact heart rate detection software based on OpenCV3.1 and Qt 5.7
**	Author : Jason.Lee
**	Date : 2017-10-17
*/

//HeartRateDetect class defintion
#ifndef _HRD_H
#define _HRD_H

// OpenCV 3.1 Library
#include<opencv2/opencv.hpp>


// Cpp System Library
#include<sstream>
#include<iomanip>
#include<vector>
#include<iostream>
#include<cstring>
#include<cstdio>
#include<cstdlib>
#include<cmath>
#include<fstream>
#include<cstddef>
#include<algorithm>
#include<chrono>
#include<ctime>
#include "opencv2/core.hpp"


#define MAX_LENGTH 250
#define ROI_ROWS 640
#define ROI_COLS 480
#define PI 3.141592653589793


using namespace std;
using namespace cv;


class HRD
{
private:
	int levels;
	float fl;
	float fh;
	float fps;
	float heartRate;
	float time;
	int LENGTH;

public:
	HRD();
	~HRD();

	bool videoGet(const char*fileName);
	void gaussPyramid();
	void idealBandPass();
	void caculateHRValue();
	float getHRValue();
	Mat Rgb2Ntsc(Mat&frame);
	float IFE_Detecting(float*bvp, float fs, int Q, int LENGTH);
	float detaQ(float*bvp, float max_IDX, int N, int Q);
	Mat fftshift(Mat&X, int length);
	int max3(float*pows, int length);
};

#endif