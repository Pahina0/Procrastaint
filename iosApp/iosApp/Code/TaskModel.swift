//
//  TaskModel.swift
//  iosApp
//
//  Created by Yi Chen on 12/10/24.
//  Copyright © 2024 orgName. All rights reserved.
//

import Foundation

struct WidgetTask: Identifiable, Codable {
    let id: UUID
    let title: String
    let dueDate: Date
    var isCompleted: Bool
}
