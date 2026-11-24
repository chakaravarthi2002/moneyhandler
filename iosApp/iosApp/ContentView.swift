import SwiftUI

struct ContentView: View {
    var body: some View {
        VStack(spacing: 20) {
            Image(systemName: "banknote.fill")
                .font(.system(size: 64))
                .foregroundColor(.blue)

            Text("Money Handler")
                .font(.largeTitle)
                .bold()

            Text("Daily Spending & Monthly Insights for iOS")
                .font(.subheadline)
                .foregroundColor(.secondary)

            Spacer()
                .frame(height: 10)

            VStack(alignment: .leading, spacing: 14) {
                HStack {
                    Image(systemName: "checkmark.circle.fill")
                        .foregroundColor(.green)
                    Text("100% Shared Compose Multiplatform UI")
                        .font(.body)
                }
                HStack {
                    Image(systemName: "checkmark.circle.fill")
                        .foregroundColor(.green)
                    Text("Auto-Category Detection (Food, Groceries, Transit)")
                        .font(.body)
                }
                HStack {
                    Image(systemName: "checkmark.circle.fill")
                        .foregroundColor(.green)
                    Text("Monthly Spending Bar Charts & Overspending Tips")
                        .font(.body)
                }
                HStack {
                    Image(systemName: "checkmark.circle.fill")
                        .foregroundColor(.green)
                    Text("Multi-Currency (USD, EUR, GBP, INR, JPY, CAD, AUD)")
                        .font(.body)
                }
            }
            .padding()
            .background(Color(.systemGray6))
            .cornerRadius(16)

            Spacer()
        }
        .padding()
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
