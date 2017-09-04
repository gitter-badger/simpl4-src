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
/**
 * A cell editor factory creating select boxes.
 */
qx.Class.define("ms123.util.SelectBox",
{
  extend : qx.core.Object,
  implement : qx.ui.table.ICellEditorFactory,


  properties :
  {
    /**
     * function that validates the result
     * the function will be called with the new value and the old value and is
     * supposed to return the value that is set as the table value.
     **/
    validationFunction :
    {
      check : "Function",
      nullable : true,
      init : null
    },

    /** array of data to construct ListItem widgets with */
    listData :
    {
      check : "Array",
      init : null,
      nullable : true
    }

  },


  members :
  {
    // interface implementation
    createCellEditor : function(cellInfo)
    {
      var cellEditor = new qx.ui.form.SelectBox().set({
        appearance: "table-editor-selectbox"
      });

      var value = cellInfo.value;
      cellEditor.originalValue = value;

      // check if renderer does something with value
      var cellRenderer = cellInfo.table.getTableColumnModel().getDataCellRenderer(cellInfo.col);
      var label = cellRenderer._getContentHtml(cellInfo);
      if ( value != label ) {
        value = label;
      }

      // replace null values
      if ( value === null ) {
        value = "";
      }

      var list = this.getListData();
      if (list)
      {
        var item;

        for (var i=0,l=list.length; i<l; i++)
        {
          var row = list[i];
          if ( row instanceof Array ) {
            item = new qx.ui.form.ListItem(row[0], row[1]);
            item.setUserData("row", row[2]);
          } else {
            item = new qx.ui.form.ListItem(row, null);
            item.setUserData("row", row);
          }
          cellEditor.add(item);
        };
      }

      var itemToSelect = cellEditor.getChildrenContainer().findItem("" + value);

      if (itemToSelect) {
        cellEditor.setSelection([itemToSelect]);
      } else {
        cellEditor.resetSelection();
      }
      cellEditor.addListener("appear", function() {
        cellEditor.open();
      });

			cellEditor.addListener("changeSelection", function() {//@@@MS
				cellInfo.table.stopEditing();
			}, this);
      return cellEditor;
    },


    // interface implementation
    getCellEditorValue : function(cellEditor)
    {
      var selection = cellEditor.getSelection();
      var value = "";

      if (selection && selection[0]) {
        value = selection[0].getUserData("row") || selection[0].getLabel();
      }

      // validation function will be called with new and old value
      var validationFunc = this.getValidationFunction();
      if (validationFunc ) {
         value = validationFunc( value, cellEditor.originalValue );
      }

      if (typeof cellEditor.originalValue == "number") {
        value = parseFloat(value);
      }

      return value;
    }
  }
});
