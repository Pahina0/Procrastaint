//
//  BottomSheetView.swift
//  iosApp
//
//  Created by Yi Chen on 8/12/24.
//  Copyright © 2024 orgName. All rights reserved.
//

import SwiftUI

struct BottomSheetView: View {
    @State private var taskDescription: String = ""
    @State private var startDate: Date = Date()
    @State private var endDate: Date = Date()
    @State private var repeatingInterval: Int = 0
    @State private var intervalUnit: String = "day"
    
    private var dateList: [String] = []
    var body: some View {
        VStack {
            // Task description field
            TextField("What's on your mind?", text: $taskDescription)
                .padding()
                .background(RoundedRectangle(cornerRadius: 10).stroke(Color.gray, lineWidth: 1))

            // Start and end time buttons
            HStack {
                HStack {
                    Image(systemName: "clock")
                        .foregroundStyle(.blue)
                    Text("Add start time")
                        .foregroundStyle(.blue)
                }
                .padding(.vertical, 8.0)
                .padding(.horizontal,5.0)
                .background(RoundedRectangle(cornerRadius: 10).stroke(Color.blue, lineWidth: 1))
                .overlay{
                    DatePicker(selection: $startDate, displayedComponents: .date) {}
                                    .labelsHidden()
                                    .contentShape(Rectangle())
                                    .opacity(0.011)
                }
                
                HStack {
                    Image(systemName: "clock")
                        .foregroundStyle(.blue)
                    Text("Edit end time")
                        .foregroundStyle(.blue)
                }
                .padding(.vertical, 8.0)
                .padding(.horizontal,5.0)
                .background(RoundedRectangle(cornerRadius: 10).stroke(Color.blue, lineWidth: 1))
                .overlay{
                    DatePicker(selection: $endDate, displayedComponents: .date) {}
                                    .labelsHidden()
                                    .contentShape(Rectangle())
                                    .opacity(0.011)
                }
                
                Spacer()
            }
            .padding(.bottom, 10)
            // Date display
            HStack {
                ForEach(dateList, id: \.self) { date in
                    Text(date)
                        .padding(8)
                        .background(RoundedRectangle(cornerRadius: 10).fill(Color.gray.opacity(0.2)))
                }
            }
            .padding(.bottom, 10)
            
           
            
            // Repeating interval
            HStack {
                Text("Repeating every")
                TextField("0", value: $repeatingInterval, formatter: NumberFormatter())
                    .keyboardType(.numberPad)
                    .frame(width: 50)
                    .padding(8)
                    .background(RoundedRectangle(cornerRadius: 10).stroke(Color.gray, lineWidth: 1))
                
                Picker(selection: $intervalUnit, label: Text("")) {
                    Text("day").tag("day")
                    Text("week").tag("week")
                    Text("month").tag("month")
                }
                .pickerStyle(MenuPickerStyle())
                Spacer()
            }
            

            // Task description field
            TextField("Description", text: $taskDescription)
                .padding()
                .background(RoundedRectangle(cornerRadius: 10).stroke(Color.gray, lineWidth: 1))
                .padding(.bottom, 10)


        }
        .padding()
        Spacer()
    }
}

#Preview {
    BottomSheetView()
}
