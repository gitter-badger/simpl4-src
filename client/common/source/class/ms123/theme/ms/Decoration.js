/*
 * This file is part of SIMPL4(http://simpl4.org).
 *
 * 	Copyright [2017] [Manfred Sattler] <manfred@ms123.org>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
qx.Theme.define("ms123.theme.ms.Decoration", {
	extend: qx.theme.modern.Decoration,

	decorations: {
	  "hover-css" : {
      decorator : [
        qx.ui.decoration.MLinearBackgroundGradient
      ],

      style : {
        startColorPosition : 0,
        endColorPosition : 100,
        startColor : "#CCCCCC",
        endColor : "#CCCCCC"
      }
    },

	  "pressed-css" : {
      decorator : [
        qx.ui.decoration.MLinearBackgroundGradient
      ],

      style : {
        startColorPosition : 0,
        endColorPosition : 100,
        startColor : "#f87925",
        endColor : "#f87925"
      }
    },
    "grid-header-cell-first" :
    {
      include : "grid-header-cell",
      style : {
        widthLeft : 1
      }
    },
    "grid-header-cell" :
    {
      decorator : qx.ui.decoration.Decorator,

      style :
      {
        widthRight : 1,
        color : "white"
      }
    }
	}
});
