//
//  TaskTimelineProvider.swift
//  iosApp
//
//  Created by Yi Chen on 12/10/24.
//  Copyright © 2024 orgName. All rights reserved.
//

import WidgetKit
import SwiftUI

struct TaskEntry: TimelineEntry {
    let date: Date
    let tasks: [WidgetTask]
}

struct TaskTimelineProvider: TimelineProvider {
    func placeholder(in context: Context) -> TaskEntry {
        TaskEntry(date: Date(), tasks: sampleTasks())
    }

    func getSnapshot(in context: Context, completion: @escaping (TaskEntry) -> Void) {
        let entry = TaskEntry(date: Date(), tasks: sampleTasks())
        completion(entry)
    }

    func getTimeline(in context: Context, completion: @escaping (Timeline<TaskEntry>) -> Void) {
        let timeline = Timeline(entries: [TaskEntry(date: Date(), tasks: sampleTasks())], policy: .atEnd)
        completion(timeline)
    }

    private func sampleTasks() -> [WidgetTask] {
        return [
            WidgetTask(id: UUID(), title: "Review upcoming exam dates", dueDate: Date().addingTimeInterval(3600), isCompleted: false),
            WidgetTask(id: UUID(), title: "Cancel free trial", dueDate: Date().addingTimeInterval(7200), isCompleted: false)
        ]
    }
}
