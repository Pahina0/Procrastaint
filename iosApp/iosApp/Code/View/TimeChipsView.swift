
import SwiftUI
import shared

public struct TimeChipsView: View {
    public var parsedTime: ParsedTime?
    
    public init(parsedTime: ParsedTime?) {
        self.parsedTime = parsedTime
    }
    
    public var body: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack {
                if let parsedTime = parsedTime {
                    ForEach(Array(parsedTime.startTimes), id: \.self) { time in
                        Text(Date(timeIntervalSince1970: TimeInterval(time.int64Value / 1000)).formatted())
                            .padding(8)
                            .background(RoundedRectangle(cornerRadius: 10).fill(Color.blue.opacity(0.2)))
                    }
                    
                    if let endTime = parsedTime.endTime {
                        Text(Date(timeIntervalSince1970: TimeInterval(endTime.int64Value / 1000)).formatted())
                            .padding(8)
                            .background(RoundedRectangle(cornerRadius: 10).fill(Color.green.opacity(0.2)))
                    }
                    
                    if parsedTime.repeatOften > 0, let repeatTag = parsedTime.repeatTag {
                        Text("Repeating every \(parsedTime.repeatOften) \(repeatTag.name.lowercased())(s)")
                            .padding(8)
                            .background(RoundedRectangle(cornerRadius: 10).fill(Color.orange.opacity(0.2)))
                    }
                }
            }
        }
    }
}
