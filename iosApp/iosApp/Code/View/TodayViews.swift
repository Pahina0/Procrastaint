//
//  TodayViews.swift
//  iosApp
//
//  Created by Yi Chen on 6/18/24.
//  Copyright © 2024 orgName. All rights reserved.
//

import SwiftUI

struct HourlyTask: Identifiable {
    let id = UUID()
    let hour: Int
    let tasks: [String] // List of tasks for that hour
}


struct TodayViews: View {
    @State private var tasksForTheDay: [HourlyTask] = [
            HourlyTask(hour: 9, tasks: ["Task 1", "Task 2"]),
            HourlyTask(hour: 14, tasks: ["Task 3"]),
            HourlyTask(hour: 17, tasks: ["Task 4", "Task 5", "Task 6"]),
        ]

    var body: some View {
        
        ZStack{
            
            VStack {
                // Title with today's date and weekday
                Text(todayDateString())
                    .font(.title)
                    
                
                // List of hours with tasks
                List(0..<24, id: \.self) { hour in
                    HStack {
                        Text(formattedHour(hour: hour))
                            .frame(width: 50, alignment: .leading)
                        
                        Divider()
                        
                        if let tasks = tasksForTheDay.first(where: { $0.hour == hour })?.tasks {
                            VStack{
                                ForEach(tasks, id: \.self) { task in
                                    Text(task)
                                        .padding(.vertical, 5)
                                        .padding(.horizontal)
                                        .background(Color.blue.opacity(0.2))
                                        .cornerRadius(5)
                                        .padding(.bottom, 5)
                                    
                                }
                            }
                            
                            
                        } else {
                            Text("No tasks")
                                .foregroundColor(.gray)
                        }
                    }
                    .padding(.vertical, 5)
                }
            }
            AddButtonView()

        }

        
    }
    private func todayDateString() -> String {
            let formatter = DateFormatter()
            formatter.dateFormat = "EEEE, MMM d"
            return formatter.string(from: Date())
    }
        
        // Function to format the hour into a 12-hour format
    private func formattedHour(hour: Int) -> String {
        let date = Calendar.current.date(bySettingHour: hour, minute: 0, second: 0, of: Date())!
        let formatter = DateFormatter()
        formatter.dateFormat = "ha"
        return formatter.string(from: date).lowercased()
    }

}




#Preview {
    TodayViews()
}
