package com.mb.task;

import android.annotation.SuppressLint;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.anastr.speedviewlib.SpeedView;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener{

    SpeedView mSpeedView;
    SeekBar mSeekBar;
    ProgressBar mProgressBar;
    EditText mEtSpeed;
    Button mBtnSubmit;
    int progressChangedValue;
    TextView mTvPlus, mTvMinus;
    ImageView mIvImage;



    private static final String TAG = "Touch";
    @SuppressWarnings("unused")
    private static final float MIN_ZOOM = 1f,MAX_ZOOM = 1f;

    // These matrices will be used to scale points of the image
    Matrix matrix = new Matrix();
    Matrix savedMatrix = new Matrix();

    // The 3 states (events) which the user is trying to perform
    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    int mode = NONE;

    // these PointF objects are used to record the point(s) the user is touching
    PointF start = new PointF();
    PointF mid = new PointF();
    float oldDist = 1f;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEtSpeed = findViewById(R.id.et_speed);
        mBtnSubmit = findViewById(R.id.btn_submit);

        mSpeedView = findViewById(R.id.speedView);
        mSeekBar = findViewById(R.id.seekbar);
        mTvMinus = findViewById(R.id.tv_minus);
        mTvPlus = findViewById(R.id.tv_plus);
        mProgressBar = findViewById(R.id.progressbar);
        mIvImage = findViewById(R.id.iv_image);
        mIvImage.setOnTouchListener(this);

        mSeekBar.setMax(100);
        mProgressBar.setMax(100);


        mBtnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String value = mEtSpeed.getText().toString().trim();
                if (!value.isEmpty()) {
                    progressChangedValue = Integer.parseInt(value);
                    mSpeedView.speedTo((float) progressChangedValue);

                    mProgressBar.setProgress(progressChangedValue);
                    mSeekBar.setProgress(progressChangedValue);
                    mEtSpeed.setText("");
                }
            }
        });
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {


            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                progressChangedValue = i;
                mSpeedView.speedTo((float) progressChangedValue);

                mProgressBar.setProgress(progressChangedValue);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
//                progressChangedValue = 0;
            }
        });

        mTvPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (progressChangedValue < 100) {
                    mTvPlus.setEnabled(true);
                    progressChangedValue = progressChangedValue + 1;
                    mSpeedView.speedTo((float) progressChangedValue);

                    mProgressBar.setProgress(progressChangedValue);
                    mTvMinus.setEnabled(true);
                } else {
                    mTvPlus.setEnabled(false);
                }
            }
        });

        mTvMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (progressChangedValue == 1) {
                    mTvMinus.setEnabled(false);
                } else {
                    progressChangedValue = progressChangedValue - 1;

                    mProgressBar.setProgress(progressChangedValue);
                    mTvMinus.setEnabled(true);
                    mSpeedView.speedTo((float) progressChangedValue);
                }
            }
        });
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        ImageView iv = (ImageView) view;
        iv.setScaleType(ImageView.ScaleType.MATRIX);
        float scale;

        dumpEvent(motionEvent);
        // Handle touch events here...

        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK)
        {
            case MotionEvent.ACTION_DOWN:   // first finger down only
                savedMatrix.set(matrix);
                start.set(motionEvent.getX(), motionEvent.getY());
                Log.d(TAG, "mode=DRAG"); // write to LogCat
                mode = DRAG;
                break;

            case MotionEvent.ACTION_UP: // first finger lifted

            case MotionEvent.ACTION_POINTER_UP: // second finger lifted

                mode = NONE;
                Log.d(TAG, "mode=NONE");
                break;

            case MotionEvent.ACTION_POINTER_DOWN: // first and second finger down

                oldDist = spacing(motionEvent);
                Log.d(TAG, "oldDist=" + oldDist);
                if (oldDist > 5f) {
                    savedMatrix.set(matrix);
                    midPoint(mid, motionEvent);
                    mode = ZOOM;
                    Log.d(TAG, "mode=ZOOM");
                }
                break;

            case MotionEvent.ACTION_MOVE:

                if (mode == DRAG)
                {
                    matrix.set(savedMatrix);
                    matrix.postTranslate(motionEvent.getX() - start.x, motionEvent.getY() - start.y); // create the transformation in the matrix  of points
                }
                else if (mode == ZOOM)
                {
                    // pinch zooming
                    float newDist = spacing(motionEvent);
                    Log.d(TAG, "newDist=" + newDist);
                    if (newDist > 5f)
                    {
                        matrix.set(savedMatrix);
                        scale = newDist / oldDist; // setting the scaling of the
                        // matrix...if scale > 1 means
                        // zoom in...if scale < 1 means
                        // zoom out
                        matrix.postScale(scale, scale, mid.x, mid.y);
                    }
                }
                break;
        }

        iv.setImageMatrix(matrix); // display the transformation on screen

        return true; // indicate event was handled
    }


    private float spacing(MotionEvent event)
    {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    private void midPoint(PointF point, MotionEvent event)
    {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }


    private void dumpEvent(MotionEvent event)
    {
        String names[] = { "DOWN", "UP", "MOVE", "CANCEL", "OUTSIDE","POINTER_DOWN", "POINTER_UP", "7?", "8?", "9?" };
        StringBuilder sb = new StringBuilder();
        int action = event.getAction();
        int actionCode = action & MotionEvent.ACTION_MASK;
        sb.append("event ACTION_").append(names[actionCode]);

        if (actionCode == MotionEvent.ACTION_POINTER_DOWN || actionCode == MotionEvent.ACTION_POINTER_UP)
        {
            sb.append("(pid ").append(action >> MotionEvent.ACTION_POINTER_ID_SHIFT);
            sb.append(")");
        }

        sb.append("[");
        for (int i = 0; i < event.getPointerCount(); i++)
        {
            sb.append("#").append(i);
            sb.append("(pid ").append(event.getPointerId(i));
            sb.append(")=").append((int) event.getX(i));
            sb.append(",").append((int) event.getY(i));
            if (i + 1 < event.getPointerCount())
                sb.append(";");
        }

        sb.append("]");
        Log.d("Touch Events ---------", sb.toString());
    }
}
