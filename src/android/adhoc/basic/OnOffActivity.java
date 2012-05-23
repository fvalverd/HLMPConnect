/*
*  This file is part of Barnacle Wifi Tether
*  Copyright (C) 2010 by Szymon Jakubczak
*
*  This program is free software: you can redistribute it and/or modify
*  it under the terms of the GNU General Public License as published by
*  the Free Software Foundation, either version 3 of the License, or
*  (at your option) any later version.
*
*  This program is distributed in the hope that it will be useful,
*  but WITHOUT ANY WARRANTY; without even the implied warranty of
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*  GNU General Public License for more details.
*
*  You should have received a copy of the GNU General Public License
*  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package android.adhoc.basic;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ToggleButton;

import android.HLMPConnect.R;
import android.adhoc.AdHocActivity;
import android.adhoc.AdHocService;


public class OnOffActivity extends AdHocActivity implements OnClickListener {
	
	private ToggleButton onoff;
    

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.onoff = (ToggleButton) this.findViewById(R.id.onoff);
        this.onoff.setOnClickListener(this);
    }
    
    public void onClick(View view) {
    	this.onoff.setPressed(true);
		if (this.onoff.isChecked()) {
			this.requestStartAdHoc();
		}
		else {
			this.requestStopAdHoc();
		}
	}

    
    protected void requestStartAdHoc() {
    	this.adHocApp.startAdHoc();
	}

	protected void requestStopAdHoc() {
		this.adHocApp.stopAdHoc();
	}

	
	@Override
    public void updateContent(int state) {
    	switch (state) {
			case AdHocService.STATE_STOPPED: case AdHocService.STATE_FAILED: {
				this.onoff.setChecked(false);
				break;
			}
			case AdHocService.STATE_STARTING: {
				this.onoff.setPressed(true);
				this.onoff.setChecked(true);
				break;
			}
			case AdHocService.STATE_RUNNING: {
				this.onoff.setPressed(false);
				this.onoff.setChecked(true);
				break;
			}
		}
    }

}