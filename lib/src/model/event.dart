import 'dart:io';

import 'package:add_2_calendar/src/model/availability.dart';
import 'package:add_2_calendar/src/model/recurrence.dart';

/// Class that holds each event's info.
class Event {
  String title, description, location;
  String? timeZone;
  DateTime startDate, endDate;
  bool allDay;
  Availability availability;

  IOSParams iosParams;
  AndroidParams androidParams;
  Recurrence? recurrence;

  Event({
    required this.title,
    this.description = '',
    this.location = '',
    required this.startDate,
    required this.endDate,
    this.timeZone,
    this.allDay = false,
    this.availability = Availability.BUSY,
    this.iosParams = const IOSParams(),
    this.androidParams = const AndroidParams(),
    this.recurrence,
  });

  Map<String, dynamic> toJson() {
    Map<String, dynamic> params = {
      'title': title,
      'desc': description,
      'location': location,
      'startDate': startDate.millisecondsSinceEpoch,
      'endDate': endDate.millisecondsSinceEpoch,
      'timeZone': timeZone,
      'allDay': allDay,
      'availability': availability.index,
      'recurrence': recurrence?.toJson(),
    };

    if (Platform.isIOS) {
      params['alarmInterval'] = iosParams.reminder?.inSeconds.toDouble();
    } else {
      params['invites'] = androidParams.emailInvites?.join(",");
    }

    return params;
  }
}

class AndroidParams {
  final List<String>? emailInvites;

  const AndroidParams({this.emailInvites});
}

class IOSParams {
  //In iOS, you can set alert notification with duration. Ex. Duration(minutes:30) -> After30 min.
  final Duration? reminder;
  const IOSParams({this.reminder});
}
