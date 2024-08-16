//
//  CalendarView.swift
//  iosApp
//
//  Created by Yi Chen on 7/29/24.
//  Copyright © 2024 orgName. All rights reserved.
//

import SwiftUI

struct CalendarView: View {
    @State private var currentMonth = Date()
        
    private var months: [Month] {
        var monthsArray = [Month]()
        for offset in -12...12 {
            if let month = Calendar.current.date(byAdding: .month, value: offset, to: currentMonth) {
                monthsArray.append(Month(month: month))
            }
        }
        return monthsArray
    }
    
    
    var body: some View {
        ScrollView {
            VStack(spacing: 20) {
                ForEach(months, id: \.month) { month in
                    VStack(alignment: .leading) {
                        Text(monthFormatted(date: month.month))
                            .font(.title)
                            .bold()
                        LazyVGrid(columns: Array(repeating: GridItem(.flexible()), count: 7), spacing: 10) {
                            ForEach(month.days) { day in
                                VStack {
                                    Text(day.dayName.prefix(3))
                                        .font(.caption)
                                    Text(day.dayNumber)
                                        .font(.title2)
                                }
                                .padding()
                                .background(Color.gray.opacity(0.2))
                                .cornerRadius(8)
                            }
                        }
                    }
                }
            }
            .padding()
        }
    }
    
    private func monthFormatted(date: Date) -> String {
            let formatter = DateFormatter()
            formatter.dateFormat = "MMMM yyyy"
            return formatter.string(from: date)
    }
}

#Preview {
    CalendarView()
}
