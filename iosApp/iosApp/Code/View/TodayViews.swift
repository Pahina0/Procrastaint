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
    var tasks: [Task] // List of tasks for that hour
}

struct Task: Identifiable,Hashable {
    let id = UUID()
    let title: String
    let description: String
}


struct TodayViews: View {
    //for UI testing only
    @State private var tasksForTheDay: [HourlyTask] = [
        HourlyTask(hour: 9, tasks: [Task(title:"Task1",description: "This is task #1"), Task(title:"Task2",description: "This is task #1")]),
            HourlyTask(hour: 14, tasks: [Task(title:"Task3",description: "")]),
            HourlyTask(hour: 17, tasks: [Task(title:"Task6",description: ""),Task(title:"Task5",description: ""), Task(title:"Task4",description: "")]),
        ]

    var body: some View {
        
        ZStack{
            
            VStack {
                // Title with today's date and weekday
                HStack{
                    Text("Today")
                        .font(.title)
                        .padding(.leading)
                    Spacer()
                }
                HStack{
                    Text(todayDateString())
                        .font(.title2)
                        .padding([.leading])
                    Spacer()
                }
                    
                
                // List of hours with tasks
                List(0..<24, id: \.self) { hour in
                    HStack {
                        Text(formattedHour(hour: hour))
                            .frame(width: 50, alignment: .leading)
                        
                        Divider()
                        
                        if let index = tasksForTheDay.firstIndex(where: { $0.hour == hour }) {
                            VStack{
                                ForEach(tasksForTheDay[index].tasks, id: \.self) { task in
                                    TaskRowView(tasks: $tasksForTheDay[index].tasks,task: task)
                            
                                }
                            }
                            
                            
                        } else {
                            Text("No tasks")
                                .foregroundColor(.gray)
                        }
                    }
                    .padding(.vertical, 5)
                }
                .listStyle(PlainListStyle())
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

struct TaskRowView: View {
    @Binding var tasks: [Task]
    var task: Task
    
    @State private var isChecked = false
    
    
    var body: some View {
        HStack {
            VStack(alignment: .leading) {
                Text(task.title)
                    .font(.headline)
                Text(task.description)
                    .font(.subheadline)
                    .foregroundColor(.secondary)
            }
            Spacer()
            Image(systemName: isChecked ? "checkmark.circle.fill" : "circle")
                .resizable()
                .frame(width: 24, height: 24)
                .foregroundColor(isChecked ? .green : .gray)
                .onTapGesture {
                    withAnimation {
                        isChecked.toggle()
                        if isChecked {
                            // Remove the task after the animation
                            if let index = tasks.firstIndex(where: { $0.id == task.id }) {
                                tasks.remove(at: index)
                            }
                        }
                    }
                }
        }
        .padding(.vertical, 8)
        .background(randomColor().opacity(0.7))
    }
    
    //for UI testing Only
    private func randomColor() -> Color{
        let red = CGFloat(drand48())
        let green = CGFloat(drand48())
        let blue = CGFloat(drand48())
        return Color(red: red, green: green, blue: blue)
    }
}



#Preview {
    TodayViews()
}
