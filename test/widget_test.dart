import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:titan_clone/app.dart';

void main() {
  testWidgets('TitanClone app renders', (WidgetTester tester) async {
    await tester.pumpWidget(
      const ProviderScope(child: TitanCloneApp()),
    );

    expect(find.text('TitanClone'), findsOneWidget);
  });
}
