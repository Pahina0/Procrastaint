//
//  iOSWidgetsBundle.swift
//  iOSWidgets
//
//  Created by Yi Chen on 11/10/24.
//  Copyright © 2024 orgName. All rights reserved.
//

import WidgetKit
import SwiftUI

@main
struct iOSWidgetsBundle: WidgetBundle {
    var body: some Widget {
        iOSWidgets()
        iOSWidgetsLiveActivity()
        ProcrastaintWidget()
    }
}
