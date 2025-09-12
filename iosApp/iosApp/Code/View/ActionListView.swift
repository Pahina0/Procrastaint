
import SwiftUI

public struct ActionListView: View {
    public var viewNextParsed: () -> Void
    public var viewing: Int
    public var currentViewingSize: Int
    
    public init(viewNextParsed: @escaping () -> Void, viewing: Int, currentViewingSize: Int) {
        self.viewNextParsed = viewNextParsed
        self.viewing = viewing
        self.currentViewingSize = currentViewingSize
    }
    
    public var body: some View {
        HStack {
            Button(action: viewNextParsed) {
                HStack {
                    Image(systemName: viewing != -1 ? "arrow.triangle.2.circlepath" : "arrow.triangle.2.circlepath.circle.fill")
                    Text(label)
                }
            }
            .padding(8)
            .background(RoundedRectangle(cornerRadius: 10).stroke(Color.blue, lineWidth: 1))
        }
    }
    
    private var label: String {
        switch viewing {
        case -1:
            return "No times found"
        case currentViewingSize:
            return "Ignored"
        default:
            return "\(viewing + 1)/\(currentViewingSize)"
        }
    }
}
