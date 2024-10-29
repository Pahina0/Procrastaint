//
//  CalendarView.swift
//  iosApp
//
//  Created by Yi Chen on 7/29/24.
//  Copyright © 2024 orgName. All rights reserved.
//

import SwiftUI



struct CalendarView: View {
    @State private var currentMonthOffset: Int = 0

    
    var body: some View {
        
        VStack(spacing: 20) {
			//month tabber
			HStack {
				//left arrow
				Button(action: {
					//scroll to left month
				}) {
					Image(systemName: "arrowtriangle.left")
						//so that size of icon changes as scree size does
						.resizable()
						.scaledToFit()
						.frame(width: UIScreen.main.bounds.width * 0.04)
						.foregroundColor(.black) // Sets the icon color
				}
				
				/*
				read in the current month and present initially,
				swipe left, goes left
				swipe right, goes right 
				 */
				
			    var nextMonthDate: String {
					//0 so that it is the current date
					let date = Calendar.current.date(byAdding: .month, value: 0, to: Date())!
					let formatter = DateFormatter()
					formatter.dateStyle = .medium
					return formatter.string(from: date)
				}
				
			
				
				Text(nextMonthDate)
				.padding()
				
				//right arrow
				Button(action: {
					//scroll to right month
				}) {
					Image(systemName: "arrowtriangle.right")
						//so that size of icon changes as scree size does
						.resizable()
						.scaledToFit()
						.frame(width: UIScreen.main.bounds.width * 0.04)
						.foregroundColor(.black) // Sets the icon color
				}
				
			}
			
            // Weekday Headers
            HStack {
                ForEach(Calendar.current.shortWeekdaySymbols, id: \.self) { weekday in
                    Text(weekday)
                        .fontWeight(.bold)
                        .frame(maxWidth: .infinity)
                }
            }
            .padding(.top)

            // Month Grid in ScrollView
            ScrollViewReader{reader in
				let numMonths: Int = 25
                ScrollView {
                //spacing in between each month
                    LazyVStack(spacing: 20) {
						// Always be an odd number so current month can be centered
						//change to 25 allows for year in andvance and previous to be seen
                        ForEach(0..<numMonths, id: \.self) { offset in
							//note that definition of struct is below
                            MonthView(monthOffset: offset - (numMonths/2))
                        }
                    }
                }
                .onAppear{
					//when it loads automatically goes to center (aka current)
                    reader.scrollTo(numMonths/2,anchor: .top)
                }
                .onTapGesture(count:2){
					//Can double tap to go to center
                    reader.scrollTo(numMonths/2,anchor: .top)
                }
                
            }
        }
        .padding()
    }
}

struct MonthView: View {
    let monthOffset: Int


	//specifies layout for each month
    var body: some View {
        VStack {
            Text(monthYearString)
                .font(.title)
                .padding()
            NavigationView{
                LazyVGrid(columns: Array(repeating: GridItem(.flexible(),spacing: .zero), count: 7), spacing: 10) {
                    ForEach(generateMonthDays(), id: \.self) { day in
                        VStack{
                            if(!day.isEmpty){
                                Color.gray.opacity(0.3).frame(height: 2.0)
                            }
                            if(Int(day) == Calendar.current.component(.day, from: Date()) && monthOffset == 0){
                                NavigationLink{
                                    DetailedDayView(month: monthString, day: day)
                                }label:{
                                    Text(day)
                                        .frame(width: 23, height: 23)
                                        .padding()
                                        .background(Circle().fill(Color.blue.opacity(0.9)))
                                        .foregroundStyle(Color.white)
                                        .cornerRadius(10)
                                }
                                
                            }else{
                                NavigationLink{
                                    DetailedDayView(month: monthString, day: day)
                                }label:{
                                    Text(day)
                                        .frame(width: 23, height: 23)
                                        .foregroundStyle(Color.black)
                                        .padding()
                                    
                                }
                                
                            }
                            
                            
                        }
                        
                    }
                    
                }
            }
        }
    }

    private var monthYearString: String {
		//offset tells which ones to calc for each
        let date = Calendar.current.date(byAdding: .month, value: monthOffset, to: Date())!
        let formatter = DateFormatter()
        formatter.dateFormat = "MMMM yyyy"
        return formatter.string(from: date)
    }
    
    private var monthString: String {
        let date = Calendar.current.date(byAdding: .month, value: monthOffset, to: Date())!
        let formatter = DateFormatter()
        formatter.dateFormat = "MMM"
        return formatter.string(from: date)
    }

    private func generateMonthDays() -> [String] {
        var days = [String]()
        let calendar = Calendar.current
        let adjustedMonth = calendar.date(byAdding: .month, value: monthOffset, to: Date())!
        let range = calendar.range(of: .day, in: .month, for: adjustedMonth)!
        let firstDayOfMonth = calendar.date(from: calendar.dateComponents([.year, .month], from: adjustedMonth))!

        // Add leading empty cells for days before the first of the month
        let firstWeekday = calendar.component(.weekday, from: firstDayOfMonth) - calendar.firstWeekday
        days.append(contentsOf: Array(repeating: "", count: firstWeekday < 0 ? firstWeekday + 7 : firstWeekday))

        // Add the days of the month
        for day in range {
            days.append("\(day)")
        }

        return days
    }
}


#Preview {
    CalendarView()
}
