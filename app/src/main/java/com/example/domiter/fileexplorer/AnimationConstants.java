/*
 * Copyright (C) 2014 George Venios
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.domiter.fileexplorer;

import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;

public class AnimationConstants {
    public static final int ANIM_DURATION = 300;
    public static final int ANIM_START_DELAY = 0;

    public static final Interpolator IN_INTERPOLATOR = new PathInterpolator(0.8F, 0, 0.2F, 1);  // slow_in_slow_out
    public static final Interpolator OUT_INTERPOLATOR = new PathInterpolator(0.8F, 0, 0.2F, 1);
}