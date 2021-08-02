package com.example.pandd;

import android.graphics.Point;
import androidx.annotation.IdRes;
import androidx.appcompat.widget.Toolbar;
import com.github.amlcurran.showcaseview.targets.Target;
import com.github.amlcurran.showcaseview.targets.ViewTarget;


public class ToolbarActionItemTarget implements Target {

    private final Toolbar toolbar;
    private final int menuItemId;

    public ToolbarActionItemTarget(Toolbar toolbar, @IdRes int itemId) {
        this.toolbar = toolbar;
        this.menuItemId = itemId;
    }

    @Override
    public Point getPoint() {
        return new ViewTarget(toolbar.findViewById(menuItemId)).getPoint();
    }

}
