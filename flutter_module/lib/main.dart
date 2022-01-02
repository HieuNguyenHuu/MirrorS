// @dart=2.9
import 'package:animate_do/animate_do.dart';
import 'package:firebase_core/firebase_core.dart';
import 'package:firebase_database/firebase_database.dart';
import 'package:firebase_database/ui/firebase_animated_list.dart';
import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter/services.dart';
import 'package:intl/intl.dart';

import 'api.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await Firebase.initializeApp();
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  MyApp({Key key}) : super(key: key);

  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    SystemChrome.setEnabledSystemUIOverlays([]);
    return MaterialApp(
      title: 'Flutter Demo',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        // This is the theme of your application.
        //
        // Try running your application with "flutter run". You'll see the
        // application has a blue toolbar. Then, without quitting the app, try
        // changing the primarySwatch below to Colors.green and then invoke
        // "hot reload" (press "r" in the console where you ran "flutter run",
        // or simply save your changes to "hot reload" in a Flutter IDE).
        // Notice that the counter didn't reset back to zero; the application
        // is not restarted.
        primarySwatch: Colors.blue,
      ),
      home: InitPage(),
    );
  }
}

class InitPage extends StatefulWidget {
  InitPage({Key key}) : super(key: key);

  @override
  _InitPageState createState() => _InitPageState();
}

class _InitPageState extends State<InitPage> {
  final Future<FirebaseApp> _initialization = Firebase.initializeApp();
  @override
  Widget build(BuildContext context) {
    return FutureBuilder(
      // Initialize FlutterFire:
      future: _initialization,
      builder: (context, snapshot) {
        // Check for errors
        if (snapshot.hasError) {
          return SomethingWentWrong();
        }

        // Once complete, show your application
        if (snapshot.connectionState == ConnectionState.done) {
          return MyHomePage();
        }

        // Otherwise, show something whilst waiting for initialization to complete
        return Loading();
      },
    );
  }
}

class SomethingWentWrong extends StatelessWidget {
  const SomethingWentWrong({Key key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Container(
      color: Colors.amber,
    );
  }
}

class Loading extends StatelessWidget {
  const Loading({Key key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Container(
      color: Colors.red,
    );
  }
}

DatabaseReference dbref = FirebaseDatabase.instance.reference();

class MyHomePage extends StatefulWidget {
  MyHomePage({Key key}) : super(key: key);

  @override
  _MyHomePageState createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  Weather currentWeather;
  Status status;
  String error = "";

  Future<void> getWeather() async {
    String _error = "";
    try {
      final _currentWeather = await API.instance.getCurrentWeather();

      setState(() {
        currentWeather = _currentWeather;
        status = Status.ACTIVE;
      });
    } catch (e) {
      setState(() {
        error = _error;
        status = Status.ERROR;
      });
      return;
    }
  }

  @override
  void initState() {
    status = Status.PENDING;
    getWeather();
    super.initState();

    dbref = FirebaseDatabase.instance.reference();
    /*dbref
        .child("MirrorS/DeviceS_MirrorS_123")
        .get()
        .then((DataSnapshot dataSnapshot) {
      print(dataSnapshot.value.toString());
    });*/
  }

  ScrollController _scrollController = ScrollController();

  _scrollToBottom() {
    _scrollController.jumpTo(_scrollController.position.maxScrollExtent);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.black,
      body: Stack(
        alignment: Alignment.center,
        children: [
          SingleChildScrollView(
            child: Column(
              children: [
                SizedBox(
                  height: 20,
                ),
                Stack(
                  children: [
                    FadeInDown(
                      duration: const Duration(seconds: 1),
                      child: Center(
                        child: RichText(
                          text: const TextSpan(
                            text: 'You Look',
                            style: TextStyle(
                              fontFamily: "Oxgen",
                              fontSize: 40,
                              color: Colors.white,
                            ),
                            children: [
                              TextSpan(
                                text: ' Awesome ',
                                style: TextStyle(
                                  fontWeight: FontWeight.bold,
                                ),
                              ),
                              TextSpan(
                                text: 'Today',
                              ),
                            ],
                          ),
                        ),
                      ),
                    ),
                    FadeInDown(
                      duration: const Duration(seconds: 1),
                      child: Padding(
                        padding: const EdgeInsets.only(left: 900),
                        child: Container(
                          height: 100,
                          width: 100,
                          child: Image.asset(
                            'assets/images/QR.png',
                          ),
                        ),
                      ),
                    ),
                  ],
                ),
                const SizedBox(
                  height: 200,
                ),
                Padding(
                  padding: const EdgeInsets.only(left: 50, right: 50),
                  child: Row(
                    crossAxisAlignment: CrossAxisAlignment.center,
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      FadeInLeft(
                        duration: const Duration(seconds: 1),
                        child: StreamBuilder<Object>(
                            stream: Stream.periodic(const Duration(seconds: 1)),
                            builder: (context, snapshot) {
                              var now = DateTime.now();
                              var formattedTime =
                              DateFormat('hh:mm').format(now);
                              var formattedTimemaker =
                              DateFormat('a').format(now);
                              var formattedDate =
                              DateFormat('EEE dd MMM').format(now);
                              return Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  Text(
                                    formattedDate,
                                    style: const TextStyle(
                                      color: Colors.white,
                                      fontFamily: "Oxgen",
                                      fontSize: 30,
                                    ),
                                  ),
                                  Text(
                                    formattedTime,
                                    style: const TextStyle(
                                      color: Colors.white,
                                      fontFamily: "Oxgen",
                                      fontSize: 100,
                                      fontWeight: FontWeight.bold,
                                    ),
                                  ),
                                  Align(
                                    alignment: Alignment.bottomCenter,
                                    child: Text(
                                      formattedTimemaker,
                                      style: const TextStyle(
                                        color: Colors.white,
                                        fontFamily: "Oxgen",
                                        fontSize: 20,
                                      ),
                                    ),
                                  ),
                                ],
                              );
                            }),
                      ),
                      FadeInRight(
                        duration: const Duration(seconds: 1),
                        child: StreamBuilder<Object>(
                            stream: Stream.periodic(const Duration(minutes: 1)),
                            builder: (context, snapshot) {
                              getWeather();
                              return Column(
                                crossAxisAlignment: CrossAxisAlignment.end,
                                children: [
                                  const Text(
                                    'Weather',
                                    style: TextStyle(
                                      color: Colors.white,
                                      fontFamily: "Oxgen",
                                      fontSize: 30,
                                    ),
                                  ),
                                  Row(
                                    mainAxisAlignment: MainAxisAlignment.start,
                                    children: [
                                      currentWeather == null
                                          ? Image.asset(
                                        "assets/images/03n@2x.png",
                                        height: 40,
                                      )
                                          : Image.network(
                                          currentWeather.getIcon(),
                                          height: 40),
                                      Text(
                                        currentWeather == null
                                            ? "27°"
                                            : currentWeather.temp.toString() +
                                            "°",
                                        style: const TextStyle(
                                          color: Colors.white,
                                          fontFamily: "Oxgen",
                                          fontSize: 100,
                                          fontWeight: FontWeight.bold,
                                        ),
                                      ),
                                    ],
                                  ),
                                  Text(
                                    currentWeather == null
                                        ? "Sky: clear sky"
                                        : "${currentWeather.main}: ${currentWeather.desc}",
                                    style: TextStyle(
                                      fontSize: 20,
                                      color: Colors.white,
                                    ),
                                  ),
                                  Text(
                                    currentWeather == null
                                        ? "Ho Chi Minh City VN"
                                        : "${currentWeather.city} ${currentWeather.country}",
                                    style: const TextStyle(
                                      fontSize: 20,
                                      color: Colors.white,
                                    ),
                                  ),
                                ],
                              );
                            }),
                      ),
                    ],
                  ),
                ),
                const SizedBox(
                  height: 400,
                ),
                FadeInUp(
                  duration: const Duration(seconds: 1),
                  child: Container(
                    height: 450,
                    child: FirebaseAnimatedList(
                        //scrollDirection: Axis.vertical,
                        //shrinkWrap: true,
                        //physics: const NeverScrollableScrollPhysics(),
                        //sort: (a, b) => (b.key.compareTo(a.key)),
                        //reverse: true,
                        //controller: _scrollController,
                        query: dbref
                            .child("MirrorS/DeviceS_MirrorS_123/notice_list"),
                        itemBuilder: (BuildContext context,
                            DataSnapshot snapshot,
                            Animation<double> animation,
                            int index) {
                          Map<dynamic, dynamic> data = snapshot.value;
                          String appname = data['appname'].toString();
                          String apptitle = data['apptitle'].toString();
                          String apptext = data['apptext'].toString();
                          return SlideTransition(
                            position: animation.drive(
                                Tween(begin: Offset(1, 0), end: Offset(0, 0))),
                            child: Padding(
                              padding: const EdgeInsets.only(
                                  top: 10, bottom: 10, left: 240, right: 240),
                              child: Container(
                                width: 30,
                                decoration: BoxDecoration(
                                  color: Colors.transparent,
                                  borderRadius: BorderRadius.circular(25.0),
                                  border: Border.all(
                                    color: Colors.white,
                                  ),
                                ),
                                child: Padding(
                                  padding: const EdgeInsets.only(
                                      top: 12, bottom: 12, left: 60, right: 30),
                                  child: Row(
                                    children: [
                                      Column(
                                        crossAxisAlignment:
                                        CrossAxisAlignment.start,
                                        mainAxisAlignment:
                                        MainAxisAlignment.spaceBetween,
                                        children: [
                                          Text(
                                            "Name",
                                            style: const TextStyle(
                                              fontFamily: "Oxgen",
                                              fontSize: 20,
                                              color: Colors.white,
                                            ),
                                          ),
                                          SizedBox(
                                            width: 20,
                                          ),
                                          Text(
                                            "Title",
                                            style: const TextStyle(
                                              fontFamily: "Oxgen",
                                              fontSize: 20,
                                              color: Colors.white,
                                            ),
                                          ),
                                          SizedBox(
                                            width: 20,
                                          ),
                                          Text(
                                            "Text",
                                            style: const TextStyle(
                                              fontFamily: "Oxgen",
                                              fontSize: 20,
                                              color: Colors.white,
                                            ),
                                          ),
                                        ],
                                      ),
                                      SizedBox(
                                        width: 10,
                                      ),
                                      Column(
                                        crossAxisAlignment:
                                        CrossAxisAlignment.start,
                                        mainAxisAlignment:
                                        MainAxisAlignment.spaceBetween,
                                        children: [
                                          Text(
                                            appname,
                                            style: const TextStyle(
                                              fontFamily: "Oxgen",
                                              fontSize: 20,
                                              color: Colors.white,
                                            ),
                                          ),
                                          SizedBox(
                                            width: 20,
                                          ),
                                          Text(
                                            apptitle,
                                            style: const TextStyle(
                                              fontFamily: "Oxgen",
                                              fontSize: 20,
                                              color: Colors.white,
                                            ),
                                          ),
                                          SizedBox(
                                            width: 20,
                                          ),
                                          Text(
                                            apptext,
                                            style: const TextStyle(
                                              fontFamily: "Oxgen",
                                              fontSize: 20,
                                              color: Colors.white,
                                            ),
                                          ),
                                        ],
                                      ),
                                    ],
                                  ),
                                ),
                              ),
                            ),
                          );
                        }),
                  ),
                ),
              ],
            ),
          )
        ],
      ),
    );
  }
}
