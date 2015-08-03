package com.example.shimoda_tomoaki.helloworld;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.support.v4.app.Fragment;

import java.util.ArrayList;

public class ImageListFragment extends Fragment {
    private static final String ARG_CATEGORY_ID = "categoryId";

    private int mCategoryId;
    private ArrayList<MyImageView> mImageViewList;
    private MyEnum mType = MyEnum.NORMAL;
    private View mRootView;

    public enum MyEnum {
        SMALL(3, 1.5, 6),
        NORMAL(2, 2.0, 4),
        LARGE(1, 3.0, 3);

        private final int mNumImage;
        private final double mScale;
        private final int mYPartitions;

        MyEnum(int numImage, double scale, int yPartitions) {
            mNumImage = numImage;
            mScale = scale;
            mYPartitions = yPartitions;
        }

        public int getNumImage() { return mNumImage; }
        public double getScale() { return mScale; }
        public int getYPartitions() { return mYPartitions; }
    }

    public OnFragmentInteractionListener mListener;

    public static ImageListFragment newInstance(int categoryId) {
        ImageListFragment fragment = new ImageListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_CATEGORY_ID, categoryId);
        fragment.setArguments(args);
        return fragment;
    }

    public ImageListFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mCategoryId = getArguments().getInt(ARG_CATEGORY_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        mRootView = inflater.inflate(R.layout.fragment_image_list, container, false);

        mImageViewList = getImageList();

        if (mImageViewList.size() == 0) {
            mRootView.findViewById(R.id.no_image_message_frame).setVisibility(View.VISIBLE);
        } else {
            setImageView();
        }

        return mRootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {}

    private void setImageView() {
        Point displaySize = new Point();
        getActivity().getWindowManager().getDefaultDisplay().getSize(displaySize);
        int minMinHeight = getMinHeight(mType);

        LayoutInflater inflater = LayoutInflater.from(getActivity());

        int minWidth = getMinWidth(mType);
        ArrayList<ArrayList<MyImageView>> imageListList = new ArrayList<>();
        for (int i = 0; i < mImageViewList.size();) {
            ArrayList<MyImageView> imageList = new ArrayList<>();
            int minHeight = Integer.MAX_VALUE;
            for (; i < mImageViewList.size();) {
                MyImageView myImageView = mImageViewList.get(i++);
                imageList.add(myImageView);
                if(myImageView.getHeight(mType) < minHeight) minHeight = myImageView.getHeight(mType);

                int sumWidth = 0;
                int height = minHeight > minMinHeight ? minHeight : minMinHeight;
                for (MyImageView eachRowImageView : imageList) {
                    sumWidth += (((double) height / (double) eachRowImageView.getHeight(mType)) * (double) eachRowImageView.getWidth(mType)) + 3;
                }
                if (imageList.size() >= mType.getNumImage() && sumWidth >= minWidth) break;
            }
            imageListList.add(imageList);
        }

        LinearLayout linearLayout = (LinearLayout) mRootView.findViewById(R.id.linearLayout);
        linearLayout.removeViews(0, linearLayout.getChildCount());
        for(ArrayList<MyImageView> imageList : imageListList) {
            int minHeight = Integer.MAX_VALUE;
            for (MyImageView imageView : imageList) {
                if (imageView.getHeight(mType) < minHeight) minHeight = imageView.getHeight(mType);
            }
            int height = minHeight > minMinHeight ? minHeight : minMinHeight;
            if (imageList.size() < mType.getNumImage() && height > getMaxHeight(mType)) height = getMaxHeight(mType);
            int sumWidth = 0;
            for (MyImageView imageView : imageList) {
                sumWidth += (int) (((double) height / (double) imageView.getHeight()) * (double) imageView.getWidth());
            }
            LinearLayout imageRowView = (LinearLayout) inflater.inflate(R.layout.image_row_view, linearLayout, false);
            imageRowView.setGravity(Gravity.CENTER_HORIZONTAL);

            int width = (int) ((double)displaySize.x - (double) ((float)(imageList.size() + 1) * 3f * getResources().getDisplayMetrics().density));
            double baseScale = width > sumWidth ? 1.0 : ((double) width / (double) sumWidth);

            for(MyImageView myImageView : imageList) {
                double scale = baseScale * ((double) height / (double) myImageView.getHeight());
                Bitmap bitmap = myImageView.getBitmap();
                ImageView imageView = new ImageView(getActivity());
                imageView = getImageView(imageView, myImageView);
                imageView.setLayoutParams(new LinearLayout.LayoutParams((int) (scale * (double) bitmap.getWidth()), (int) (scale * (double) bitmap.getHeight())));
                imageRowView.addView(imageView);
            }

            linearLayout.addView(imageRowView);
        }
        if(mType != MyEnum.LARGE && linearLayout.getChildCount() != 0) ((LinearLayout) linearLayout.getChildAt(linearLayout.getChildCount()-1)).setGravity(Gravity.LEFT);
    }

    public ArrayList<MyImageView> getImageList() {
        ArrayList<MyImageView> imageViewList = new ArrayList<>();

        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase("data/data/" + getActivity().getPackageName() + "/Sample.db", null);
        Cursor cursor = db.query("image", new String[]{"_id", "categoryId", "image", "created_date"}, "categoryId = ?", new String[]{"" + mCategoryId}, null, null, "_id");

        while (cursor.moveToNext()) {
            final byte blob[] = cursor.getBlob(cursor.getColumnIndex("image"));
            final Bitmap bitmap = BitmapFactory.decodeByteArray(blob, 0, blob.length);

            imageViewList.add(new MyImageView(cursor.getInt(cursor.getColumnIndex("_id")), bitmap));
        }
        db.close();

        return imageViewList;
    }

    public ImageView getImageView(ImageView imageView, final MyImageView myImageView) {
        final Bitmap bitmap  = myImageView.getBitmap();
        imageView.setImageBitmap(bitmap);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Point displaySize = new Point();
                getActivity().getWindowManager().getDefaultDisplay().getSize(displaySize);

                final FrameLayout frameLayout = (FrameLayout) mRootView.findViewById(R.id.dialog_frame_layout);
                ImageView imageView = (ImageView) mRootView.findViewById(R.id.imageView4);
                Button button = (Button) mRootView.findViewById(R.id.button);

                frameLayout.setVisibility(View.VISIBLE);
                frameLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        frameLayout.setVisibility(View.GONE);
                    }
                });

                imageView.setImageBitmap(bitmap);
                double scale;
                if ((bitmap.getHeight() / displaySize.y) > (bitmap.getWidth() / displaySize.x)) {
                    int imageHeight = displaySize.y < 4 * bitmap.getHeight() ? frameLayout.getHeight() : 4 * bitmap.getHeight();
                    scale = (double) imageHeight / (double) bitmap.getHeight();
                } else {
                    int imageWidth = displaySize.x < 4 * bitmap.getWidth() ? displaySize.x : 4 * bitmap.getWidth();
                    scale = (double) imageWidth / (double) bitmap.getWidth();
                }
                imageView.setLayoutParams(new FrameLayout.LayoutParams((int) (scale * (double) bitmap.getWidth()), (int) (scale * (double) bitmap.getHeight()), Gravity.CENTER));

                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    }
                });

                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SQLiteDatabase db = DBTools.getDatabase(getActivity());
                        db.delete("image", "_id = ? AND categoryId = ?", new String[]{"" + myImageView.getId(), "" + mCategoryId});
                        db.close();
                        mImageViewList.remove(myImageView);
                        setImageView();
                        frameLayout.setVisibility(View.GONE);

                        if (mImageViewList.size() == 0) {
                            mRootView.findViewById(R.id.no_image_message_frame).setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        });

        return imageView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.menu_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch(id) {
            case R.id.action_settings1: mType = MyEnum.SMALL; break;
            case R.id.action_settings2: mType = MyEnum.NORMAL; break;
            case R.id.action_settings3: mType = MyEnum.LARGE; break;
        }

        setImageView();

        return super.onOptionsItemSelected(item);
    }

    private int getMinHeight(MyEnum type) {
        Point displaySize = new Point();
        getActivity().getWindowManager().getDefaultDisplay().getSize(displaySize);

        return displaySize.y / type.getYPartitions();
    }

    private int getMinWidth(MyEnum type) {
        Point displaySize = new Point();
        getActivity().getWindowManager().getDefaultDisplay().getSize(displaySize);

        if (type == MyEnum.SMALL) {
            return displaySize.x;
        } else if  (type == MyEnum.NORMAL) {
            return 2 * displaySize.x / 3;
        } else {
            return displaySize.x / 2;
        }
    }

    private int getMaxHeight(MyEnum type) {
        Point displaySize = new Point();
        getActivity().getWindowManager().getDefaultDisplay().getSize(displaySize);

        if (type == MyEnum.SMALL) {
            return displaySize.y / 6;
        } else if  (type == MyEnum.NORMAL) {
            return displaySize.y / 4;
        } else {
            return displaySize.y;
        }
    }

    public boolean cancelPreviewMode() {
        final FrameLayout frameLayout = (FrameLayout) mRootView.findViewById(R.id.dialog_frame_layout);
        if (frameLayout.getVisibility() == View.VISIBLE) {
            frameLayout.setVisibility(View.GONE);
            return true;
        }
        return false;
    }

    public class MyImageView {
        private int mId;
        private Bitmap mBitmap;

        MyImageView(int id, Bitmap bitmap) {
            mId = id;
            mBitmap = bitmap;
        }

        public int getId() { return mId; }
        public Bitmap getBitmap() { return mBitmap; }
        public int getHeight() { return mBitmap.getHeight(); }
        public int getWidth() { return mBitmap.getWidth(); }
        public int getHeight(MyEnum type) { return (int)(type.getScale() * (double)mBitmap.getHeight()); }
        public int getWidth(MyEnum type) { return (int)(type.getScale() * (double)mBitmap.getWidth()); }
    }
}
