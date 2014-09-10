package com.tonalan.xkcdreader.data;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.widget.Toast;

import com.tonalan.xkcdreader.Viewer;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;

public class XKCDImageView extends ImageViewTouch {

    private String text;

    public XKCDImageView(Context context, BitmapDrawable image,  String _text) {
        super(context);

        text = _text;

        setImageDrawable(image);
        setSingleTapListener(new OnImageViewTouchSingleTapListener() {
            @Override
            public void onSingleTapConfirmed() {
                Viewer.toast.makeText(getContext(), text, Toast.LENGTH_LONG);
            }
        });
    }
}
