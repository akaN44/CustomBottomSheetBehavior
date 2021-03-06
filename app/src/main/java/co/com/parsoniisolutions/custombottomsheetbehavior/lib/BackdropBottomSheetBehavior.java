package co.com.parsoniisolutions.custombottomsheetbehavior.lib;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.NestedScrollingChild;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import co.com.parsoniisolutions.custombottomsheetbehavior.R;

/**
 * This behavior depends on a NestedScrollingChild.
 * It will move the view on which its attached proportionally to the dependency (NestedScrollingChild)
 * creating a parallax effect between the two views.
 *
 *  -------------------> Top Screen
 *  | Toolbar Height| (And status bar height if >= Lollipop)
 *  -------------------> Bottom ToolBar (Start Child Y if screen rotation while at anchor point or expanded)
 *  |               |
 *  |               |
 *  |               |
 *  |               |
 *  -------------------> AnchorPoint Y
 *  _               _
 *  | Dependency    |
 *  | Scroll Range  |
 *  | this behavior |
 *  | react to      |
 *  | (moving child)|
 *  -------------------> Collapsed Y (Start Child Y)
 *  | Peek Height   |
 *  -------------------> Bottom Screen
 *  -               -
 * @param <V> The View on which the Behavior is attached.
 */
public class BackdropBottomSheetBehavior<V extends View> extends CoordinatorLayout.Behavior<V> {

    private static final String TAG = BackdropBottomSheetBehavior.class.getSimpleName();
    /**
     * BackdropBottomSheetBehavior init state
     */
    boolean mInit = false;
    /**
     *  The Y value of the dependency (BottomSheet) when collapsed
     */
    int mCollapsedY;
    /**
     *  The peek height value of the dependency (BottomSheet)
     */
    int mPeekHeight;
    /**
     * The Y value of the Anchor point
     */
    int mAnchorPointY;
    /**
     * The current Y value of the child (BackDrop View)
     */
    int mCurrentChildY;

    public BackdropBottomSheetBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a =
                context.obtainStyledAttributes(attrs, R.styleable.BackdropBottomSheetBehavior_Params);
        setPeekHeight(
                a.getDimensionPixelSize(
                        R.styleable.BackdropBottomSheetBehavior_Params_behavior_backdrop_peekHeight,0));
        a.recycle();
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        return dependency instanceof NestedScrollingChild;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
        if(!mInit){
            init(child, dependency);
            return false;
        }
        child.setY(getCurrentChildY(dependency.getY()));
        return true;
    }

    /**
     * Get the current child Y depending on the dependency Y
     * @param dependencyY the dependency to react on
     * @return the current child Y or 0 if the child already reached the top
     */
    private int getCurrentChildY(float dependencyY){
        return (mCurrentChildY = (int) ((dependencyY-mAnchorPointY) * mCollapsedY / (mCollapsedY-mAnchorPointY)))
                <= 0 ? 0 : mCurrentChildY;
    }

    /**
     * Init the behavior
     * @param child the view attached to this behavior
     * @param dependency the dependency to react on
     */
    private void init(@NonNull View child, @NonNull View dependency){

        // The dependency Y value is equal to the height of the dependency (BottomSheet) which is the size of the screen
        // less two time the size of the peek height of the dependency (BottomSheet) corresponding to
        // the bottom sheet visible part in collapsed mode plus the toolbar (and status bar after Lollipop)
        // Both should be equal
        mCollapsedY = dependency.getHeight() - (2 * mPeekHeight);

        //The Anchor point Y value is corresponding to the child (ImageView or Viewpager) Height
        mAnchorPointY = child.getHeight();

        if(mCollapsedY < mAnchorPointY)
            Log.w(TAG,"The Anchor Point is to high regarding the side of the screen");

        // If the current child Y value is equal to the Collapsed Y plus the Peek Height value,
        // it's mean that the dependency is hidden
        // otherwise it's a screen rotation and the dependency was at anchor point or expanded before the rotation
        child.setY((mCurrentChildY = (int) dependency.getY()) == mCollapsedY + mPeekHeight ?
                    mCurrentChildY : (mCurrentChildY = 0));
        mInit = true;
    }

    /**
     * Set the peek Height value
     * @param peekHeight the peek Height value
     */
    public void setPeekHeight(int peekHeight) {
        this.mPeekHeight = peekHeight;
    }
}
