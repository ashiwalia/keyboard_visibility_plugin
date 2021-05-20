import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:keyboard_visibility_plugin/keyboard_visibility_plugin.dart';

void main() {
  const MethodChannel channel = MethodChannel('keyboard_visibility_plugin');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await KeyboardVisibilityPlugin.platformVersion, '42');
  });
}
