import SwiftUI
import shared

struct ContentView: View {
    
    @ObservedObject var dateManager = DateManager()
    
    var body: some View {
        VStack {
            // Header with the month and year
            Text(dateManager.monthYearString())
                .font(.largeTitle)
                .padding()
            
            // Grid of days
            LazyVGrid(columns: Array(repeating: GridItem(.flexible()), count: 7)) {
                ForEach(dateManager.daysInMonth, id: \.self) { date in
                    VStack {
                        Text(dateManager.dayString(date: date))
                            .frame(width: 40, height: 40)
                            .background(Color.gray.opacity(0.2))
                            .cornerRadius(10)
                        
                        // Placeholder for tasks (customize as needed)
                        
                    }
                }
            }
            .padding()
            
            // Swipe Gestures
            .gesture(DragGesture()
                .onEnded { value in
                    if value.translation.width < 0 {
                        // Swipe left, move to the next month
                        dateManager.moveToNextMonth()
                    } else if value.translation.width > 0 {
                        // Swipe right, move to the previous month
                        dateManager.moveToPreviousMonth()
                    }
                }
            )
        }
    }
}



struct ContentView_Previews: PreviewProvider {
	static var previews: some View {
		ContentView()
	}
}
