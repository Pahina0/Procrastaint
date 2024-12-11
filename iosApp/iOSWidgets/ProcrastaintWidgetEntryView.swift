//
//  ProcrastaintWidgetEntryView.swift
//  iosApp
//
//  Created by Yi Chen on 12/10/24.
//  Copyright © 2024 orgName. All rights reserved.
//

import Foundation
import SwiftUI
import WidgetKit

struct ProcrastaintWidgetEntryView: View {
    let entry: TaskEntry

    var body: some View {
        VStack(alignment: .leading) {
            Text("Upcoming")
                .font(.headline)
                .foregroundColor(.red)

            ForEach(entry.tasks.prefix(2)) { task in
                HStack {
                    Button(action: {
                        markTaskCompleted(task.id)
                    }) {
                        Circle()
                            .strokeBorder(task.isCompleted ? Color.green : Color.blue, lineWidth: 2)
                            .frame(width: 20, height: 20)
                    }

                    Text(task.title)
                        .lineLimit(1)
                        .foregroundColor(.white)

                    Spacer()

                    Text(task.dueDate, style: .date)
                        .foregroundColor(.red)
                }
            }
        }
        .padding()
    }

    private func markTaskCompleted(_ taskId: UUID) {
        // Communicate with the app to update task status
        WidgetCenter.shared.reloadAllTimelines()
    }
}
