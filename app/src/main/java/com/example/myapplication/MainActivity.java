package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

        JavaCameraView javaCameraView;
        File cascFile;

        CascadeClassifier faceDetector;
        private Mat mRgba;
    private Mat mGrey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

       /* if(OpenCVLoader.initDebug()) Log.d("LOADED","SUCCESS");
        else Log.d("LOADED","error");   */

        OpenCVLoader.initDebug();

        javaCameraView = (JavaCameraView) findViewById(R.id.javaCamView);
        if (! OpenCVLoader.initDebug())
        {
            OpenCVLoader.initAsync (OpenCVLoader.OPENCV_VERSION_3_0_0 , this, baseCallback);
        }
        else
        {
            baseCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }

        javaCameraView.setCvCameraViewListener((CameraBridgeViewBase.CvCameraViewListener) this);


    }

    public void onCameraViewStarted (int width, int height) {
        mRgba = new Mat();
        mGrey = new Mat();

    }

    public void onCameraViewStopped () {
        mRgba.release();
        mGrey.release();
    }


    public Mat onCameraFrame (CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        mRgba = inputFrame.rgba();
        mGrey = (Mat) inputFrame.gray();

        // detect Face

        MatOfRect faceDetections = new MatOfRect();
        faceDetector.detectMultiScale(mRgba,faceDetections);

        for(Rect rect: faceDetections.toArray())
        {
            Imgproc.rectangle(mRgba,new Point(rect.x,rect.y),
            new Point(rect.x+rect.width,rect.y+rect.height),
            new Scalar(255,0,0));
        }
        return null;
    }

    private BaseLoaderCallback baseCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status){
                case LoaderCallbackInterface.SUCCESS:
                {
                    InputStream is = getResources().openRawResource(R.raw.haarcascade_frontalface_alt2);
                    File casecadeDir = getDir("casecade", Context.MODE_PRIVATE) ;
                    cascFile = new File(casecadeDir,"haarcascade_frontalface_alt2.xml");

                    FileOutputStream fos = null;
                    try {
                        fos = new FileOutputStream(cascFile);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                    byte[] buffer = new byte[4096];
                    int bytesRead = 0;

                    while (true){
                        try {
                            if (!((bytesRead = is.read(buffer))!=-1)) break;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            fos.write(buffer,0,bytesRead);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    faceDetector = new CascadeClassifier(cascFile.getAbsolutePath());

                    if(faceDetector.empty()){
                        faceDetector = null;
                    }
                    else {
                        casecadeDir.delete();
                    }

                    javaCameraView.enableView();

                }
                break;
                default:
                {
                    super.onManagerConnected(status);
                }
                break;
            }

            super.onManagerConnected(status);
        }
    };
}