/**
 * This file is part of Words With Crosses.
 *
 * Copyright (C) 2009-2010 Robert Cooper
 * Copyright (C) 2013 Adam Rosenfield
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.adamrosenfield.wordswithcrosses;

import java.util.Calendar;
import java.util.GregorianCalendar;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.TextView;

import com.adamrosenfield.wordswithcrosses.net.derstandard.DateUtil;

/**
 * Custom dialog for choosing puzzles to download.
 */
public class BulkDownloadPickerDialogBuilder extends AbstractDownloadPickerDialogBuilder {
    private DatePicker fromDatePicker;
    private DatePicker toDatePicker;
    
    public BulkDownloadPickerDialogBuilder(BrowseActivity activity, final OnDownloadSelectedListener downloadButtonListener, int year, int monthOfYear, int dayOfMonth) {
        super(activity, downloadButtonListener, R.layout.bulk_download_dialog, R.id.bulk_download_root);

        OnDateChangedListener fdcl = new DatePicker.OnDateChangedListener() {
          public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
              String curPuzzle = mPuzzleSelect.getSelectedItem().toString();
              Calendar c = new GregorianCalendar(year, monthOfYear, dayOfMonth);
              updateDateLabel(R.id.fromDateWeekdayLabel, c);
          }
      };
      
      OnDateChangedListener tdcl = new DatePicker.OnDateChangedListener() {
        public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            String curPuzzle = mPuzzleSelect.getSelectedItem().toString();
            Calendar c = new GregorianCalendar(year, monthOfYear, dayOfMonth);
            updateDateLabel(R.id.toDateWeekdayLabel, c);
        }
    };

        
        toDatePicker = (DatePicker)layout.findViewById(R.id.toDatePicker);
        toDatePicker.init(year, monthOfYear, dayOfMonth, tdcl);
        tdcl.onDateChanged(toDatePicker, year, monthOfYear, dayOfMonth);
        
        if (monthOfYear > 1) {
            monthOfYear -= 1;
        } else {
            monthOfYear = 12;
            year -= 1;
        }

        fromDatePicker = (DatePicker)layout.findViewById(R.id.fromDatePicker);
        fromDatePicker.init(year, monthOfYear, dayOfMonth, fdcl);
        fdcl.onDateChanged(fromDatePicker, year, monthOfYear, dayOfMonth);
        
    }

    @Override
    protected OnClickListener createDownloadButtonClickListener() {
        return new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Calendar from = getDate(fromDatePicker);
                Calendar to = getDate(toDatePicker); 

                downloadButtonListener.onDownloadSelected(from, to, getSelectedDownloaders());
            }

            private Calendar getDate(DatePicker datePicker) {
                Calendar from = new GregorianCalendar(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
                return from;
            }
        };
    }

    @Override
    protected Calendar getCurrentDate() {
        // no filtering of available puzzles by weekday
        return null;
    }

    private void updateDateLabel(int id, Calendar date) {
    	TextView label = (TextView)layout.findViewById(id);
      label.setText(DATE_FORMAT.format(date.getTime()));
  }


}
