import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Android Native Integration',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.deepPurple),
        useMaterial3: true,
      ),
      home: const MyHomePage(title: 'Android Native Integration'),
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({super.key, required this.title});

  final String title;

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  static const platform = MethodChannel('com.example.androidinvs/crash_logs');
  String _logs = 'No logs available';
  bool _isLoading = false;

  Future<void> _getCrashLogs() async {
    setState(() {
      _isLoading = true;
    });

    try {
      final String result = await platform.invokeMethod('getCrashLogs');
      setState(() {
        _logs = result;
        _isLoading = false;
      });
    } on PlatformException catch (e) {
      setState(() {
        _logs = "Failed to get crash logs: ${e.message}";
        _isLoading = false;
      });
    }
  }

  Future<void> _getAdbLogs() async {
    setState(() {
      _isLoading = true;
    });

    try {
      final String result = await platform.invokeMethod('getAdbLogs');
      setState(() {
        _logs = result;
        _isLoading = false;
      });
    } on PlatformException catch (e) {
      setState(() {
        _logs = "Failed to get ADB logs: ${e.message}";
        _isLoading = false;
      });
    }
  }

  Future<void> _openNativeActivity() async {
    try {
      await platform.invokeMethod('openNativeActivity');
    } on PlatformException catch (e) {
      setState(() {
        _logs = "Failed to open native activity: ${e.message}";
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor: Theme.of(context).colorScheme.inversePrimary,
        title: Text(widget.title),
      ),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: <Widget>[
            ElevatedButton(
              onPressed: _openNativeActivity,
              child: const Text('Open Native Android Activity'),
            ),
            const SizedBox(height: 16),
            Row(
              children: [
                Expanded(
                  child: ElevatedButton(
                    onPressed: _getCrashLogs,
                    child: const Text('Get Crash Logs'),
                  ),
                ),
                const SizedBox(width: 8),
                Expanded(
                  child: ElevatedButton(
                    onPressed: _getAdbLogs,
                    child: const Text('Get ADB Logs'),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 16),
            const Text(
              'Logs:',
              style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16),
            ),
            const SizedBox(height: 8),
            Expanded(
              child: _isLoading
                  ? const Center(child: CircularProgressIndicator())
                  : Container(
                      padding: const EdgeInsets.all(8.0),
                      decoration: BoxDecoration(
                        border: Border.all(color: Colors.grey),
                        borderRadius: BorderRadius.circular(8.0),
                      ),
                      child: SingleChildScrollView(
                        child: Text(_logs),
                      ),
                    ),
            ),
          ],
        ),
      ),
    );
  }
}
