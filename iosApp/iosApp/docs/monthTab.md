
Task- 	Support different years between calendar, so you can swap between the 3ish year span 



Summary: 




Plan: 

	Main editing will occur in the CalendarView File 
	Resize the top of the Calendar to include a Month Tabber 
		will tell you 'Oct 2024' with a left arrow and right arrow that you can press or swipe to go to the next one 
		when you swipe, it moves the main view area vertically to the specified date 
		See figma plan in folder 
	Will also Expand the range of the calender to go on through multiple years; being able to display more than only 12 months at a time 
	

Continue work on the top swipe bar. Add functionality to the left and right arrow buttons. Figure out how to link the indexing from the calendar for loop to the above H-stack which is currently out of scope. Also add center portion of month tabber.


Before submit checklist: 



Questions: 



Outline: 



Debug: 



Things I learned: 

	Include this header on all new files: 
		//
		//  DayModel.swift
		//  iosApp
		//
		//  Created by Joe Fodera 10/25/2024.
		//  Copyright © 2024 orgName. All rights reserved.
		//

	//Previews the specific button or UI element without needing to setup an entire preview provider
		#Preview {
			BottomSheetView()
		}

	The 'list.bullet.clipboard' can be found in the sf symbols app I dowloaded, these are preloaded onto any IOS device and therefore
	do not need to import a library to use them: 
		.tabItem { Label("Today", systemImage: "list.bullet.clipboard") }
		
	I am working in SWIFTU rn. 
		swift is for the background logic while swiftUI is for the GUI . 


Any challenges I came across: 

Citations:




Current:

	Trying to figure out how to get the id of the current month up to the h-stack so that on a button press I can move to the next month: 
		will probably use: @State private var targetIndex: Int?
		
	Swiping through dummy cards 
	swipe through actual months and connect them to ones in seperate view
	
	Month Tabber complete !!!
	
	
	

Future Additions: 
	have the last month shown actually float to top 

Helpful Code Chunks: 



