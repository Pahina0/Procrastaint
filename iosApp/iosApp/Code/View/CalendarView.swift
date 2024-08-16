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
                ScrollView {
                    LazyVStack(spacing: 20) {
                        ForEach(0..<12, id: \.self) { offset in
                            MonthView(monthOffset: offset - 6)
                        }
                    }
                }
                .onAppear{
                    
                    reader.scrollTo(6,anchor: .top)
                }
                .onTapGesture(count:2){
                    reader.scrollTo(6,anchor: .top)
                }
                
            }
        }
        .padding()
    }
}

struct MonthView: View {
    let monthOffset: Int

    var body: some View {
        VStack {
            Text(monthYearString)
                .font(.title)
                .padding()

            LazyVGrid(columns: Array(repeating: GridItem(.flexible(),spacing: .zero), count: 7), spacing: 10) {
                ForEach(generateMonthDays(), id: \.self) { day in
                    VStack{
                        if(!day.isEmpty){
                            Color.gray.opacity(0.3).frame(height: 2.0)
                        }
                        if(Int(day) == Calendar.current.component(.day, from: Date()) && monthOffset == 0){
                                Text(day)
                                    .frame(width: 23, height: 23)
                                    .padding()
                                    .background(Circle().fill(Color.blue.opacity(0.9)))
                                    .foregroundStyle(Color.white)
                                    .cornerRadius(10)
                                
                        }else{
                            Text(day)
                                .frame(width: 23, height: 23)
                                .padding()
                                
                        }
                        

                    }
                    
                }
                
            }
        }
    }

    private var monthYearString: String {
        let date = Calendar.current.date(byAdding: .month, value: monthOffset, to: Date())!
        let formatter = DateFormatter()
        formatter.dateFormat = "MMMM yyyy"
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
