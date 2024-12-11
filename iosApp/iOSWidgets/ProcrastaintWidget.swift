//
//  ProcrastaintWidget.swift
//  iosApp
//
//  Created by Yi Chen on 11/10/24.
//  Copyright © 2024 orgName. All rights reserved.
//

import Foundation
import WidgetKit
import SwiftUI


struct ProcrastaintWidget: Widget {
    let kind: String = "ProcrastaintWidget"

    var body: some WidgetConfiguration {
        StaticConfiguration(kind: kind, provider: TaskTimelineProvider()) { entry in
            ProcrastaintWidgetEntryView(entry: entry)
        }
        .configurationDisplayName("Upcoming Tasks")
        .description("View and manage upcoming tasks.")
        .supportedFamilies([.systemSmall, .systemMedium])
    }
}
