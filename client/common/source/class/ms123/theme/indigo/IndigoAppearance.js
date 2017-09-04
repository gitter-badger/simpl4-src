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
qx.Theme.define("ms123.theme.indigo.IndigoAppearance",
{
  extend : qx.theme.indigo.Appearance,

  appearances :
  {
		"datefield/popup" : {
				alias : "condition/data_date"
		},
    "toolbar-button" :
    {
      alias : "atom",

      style : function(states)
      {
        var decorator;
				var padding = [3,3];
        if (
          states.pressed ||
          (states.checked && !states.hovered) ||
          (states.checked && states.disabled))
        {
          decorator = "pressed";
        } else if (states.hovered && !states.disabled) {
          decorator = "hover";
        }

        var useCSS = qx.core.Environment.get("css.gradients") &&
          qx.core.Environment.get("css.borderradius");
        if (useCSS && decorator) {
          decorator += "-css";
        }

        return {
          marginTop : 2,
          marginBottom : 2,
          padding : padding,
          decorator : decorator
        };
      }
    },
 
    "collapsable-panel" : {
      style : function(states) {
        return {
          decorator  : "pane",
          padding    : 5,
          allowGrowY : !!states.opened || !!states.horizontal,
          allowGrowX : !!states.opened ||  !states.horizontal
        };
      }
    },

    "collapsable-panel/bar" : {
      include : "groupbox/legend",
      alias   : "groupbox/legend",
      style   : function(states) {
        return {
          icon       :  states.opened ? "decoration/tree/open.png" : "decoration/tree/closed.png",
          allowGrowY : !states.opened && !!states.horizontal,
          allowGrowX :  states.opened ||  !states.horizontal,
          maxWidth   : !states.opened && !!states.horizontal ? 16 : null
        };
      }
    },

    "collapsable-panel/container" : {
      style : function(states) {
        return { padding : [0, 5] };
      }
    }

  }
});
