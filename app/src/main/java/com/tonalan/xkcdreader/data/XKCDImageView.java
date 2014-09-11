package com.tonalan.xkcdreader.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.Toast;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;

public class XKCDImageView extends ImageViewTouch {

    private String text;

    public XKCDImageView(Context context, Bitmap image,  String _text) {
        super(context);

        text = _text;

        setImageBitmap(image);
        setDisplayType(DisplayType.FIT_TO_SCREEN);
        setSingleTapListener(new OnImageViewTouchSingleTapListener() {
            @Override
            public void onSingleTapConfirmed() {
                Toast.makeText(getContext(), text, Toast.LENGTH_LONG).show();
            }
        });
    }
}
