//
//  DayModel.swift
//  iosApp
//
//  Created by Yi Chen on 8/16/24.
//  Copyright © 2024 orgName. All rights reserved.
//

import Foundation
import SwiftUI

struct Day: Identifiable {
    let id = UUID()
    let date: Date
    let dayNumber: String
    let dayName: String
}

struct Month {
    let month: Date
    var days: [Day] = []
    
    init(month: Date) {
        self.month = month
        self.days = generateDays(for: month)
    }
    
    private func generateDays(for date: Date) -> [Day] {
        var calendar = Calendar.current
        calendar.firstWeekday = 1 // Sunday = 1, Monday = 2, etc.
        
        let range = calendar.range(of: .day, in: .month, for: date)!
        var days = [Day]()
        
        for day in range {
            let components = DateComponents(year: calendar.component(.year, from: date),
                                            month: calendar.component(.month, from: date),
                                            day: day)
            let date = calendar.date(from: components)!
            let dayName = calendar.weekdaySymbols[calendar.component(.weekday, from: date) - 1]
            days.append(Day(date: date, dayNumber: "\(day)", dayName: dayName))
        }
        
        return days
    }
}
