# ActivityReport

## Services

Here are the list of services provided by ActivityReport.  

1. [RawActivity](#rawactivity-service)
2. [ActivitySummary](#activitysummary-service)
3. [GetSequences](#getsequences-service)(not actively maintained)
4. [FixTracking](#fixtracking-service)(not actively maintained)

Services listed here can be accessed using the following route:  
/ActivityReport/[service-name]?[parameters]  

**Note:** Not actively maintained services do not cover newly added content types.  

### RawActivity Service

This service generates a file with each row corresponding to one action in applications (e.g. KT portal, Quizjet, Webex, Sqlknot, MasteryGrid, etc.). It contains columns with information including topicname, activityname, durationseconds, etc.  

#### RawActivity Parameters

* **grp** (required): To specify a group or a set of groups to retrieve the data associated with. Use comma to separate multiple groups (without space), e.g. grp=group1,group2
* **header** (optional, [yes,no], default=no): If yes, the names of columns are included to the response, e.g. header=yes
* **delimiter** (optional, default=,):The delimiter to separate columns, e.g. delimiter=,
* **fromDate** (optional): Restrict data with date > *fromDate*. Format is YYYY-MM-DD HH:SS:ss, e.g. fromDate=2020-06-01
* **toDate** (optional): Restrict data with date < *toDate*. Format is YYYY-MM-DD HH:SS:ss, e.g. toDate=2020-06-01
* **filename** (optional, default=group_ids_raw_activity.txt): Name of the output file.  
* **svc** (optional, [yes,no], default=no): To include SVC field stored with user activities to the output. It varies based on the content type.
* **allparameters** (optional, [yes,no], default=no): To include allparameters field stored with user activities to the output.
* **removeUsr** (optional): Specify user(s) to be removed, comma-separated, e.g. removeUsr=usr1,usr2. Users listed in Common.non_students are automatically removed from the output.
* **excludeApp** (optional): Specify comma-separated application ids to be removed from the output, e.g. excludeApp=23,35.
* **sessionate** (optional): Include this parameter to re-do session labeling according to the time threshold specified by the *minthreshold* parameter. Sessions will be labelled from 0 and increase one everytime the next activity is performed after previous_activity_time + minthreshold.
* **minthreshold** (optional,default=90): Threshold time in minutes between sessions. If not provided and sessionate parameter is provided, default value for the threshold is 90 minutes.
* **timelabels** (optional): Include 2 or 3 labels separate by comma (e.g.: short,long or short,medium,long). If provided, the service will compute time distributions of each activity (for the given group(s)) and will label according to median or percentiles 33.3. and 66.7 (depending on how many labels are passed),
* **replaceexttimes** (optional): Include this parameter to replace the extremely long activity durations (as defined in Common.MAX_ACTIVITY_TIME) by the group-activity median time. Only works if timelabels are included (for example timelabels=s,l)
* **jsonOutput** (optional,[yes,no],default=no): To get the output as a JSON output instead of the default CSV format.  
* **queryArchive** (optional, [yes,no],default=no): To indicate if archive should be queried. If your study is not very old, you can ignore this parameter.

### ActivitySummary Service

This service is for getting summary per student of a group, it contains the total # for specific activities (question_attempts, example_line_clicks, etc.) for each user.

#### ActivitySummary Parameters

* **grp** (required): To specify a group or a set of groups to retrieve the data associated with. Use comma to separate multiple groups (without space), e.g. grp=group1,group2
* **header** (optional, [yes,no], default=no): If yes, the names of columns are included to the response, e.g. header=yes
* **filename** (optional, default=group_ids_summary.txt): Name of the output file.  
* **usr**: To specify a user or a set of users to retrieve the data associated with. The output will keep the given user order, e.g. usr=usr1,usr2. Given users should be registered in the specified groups.
* **fromDate** (optional): Restrict data with date > *fromDate*. Format is YYYY-MM-DD HH:SS:ss, e.g. fromDate=2020-06-01
* **toDate** (optional): Restrict data with date < *toDate*. Format is YYYY-MM-DD HH:SS:ss, e.g. toDate=2020-06-01
* **timebins** (optional): Comma-separated unixtimestamp values to divide the summary output into different time-bins. If provided, the output will include details related to each time-bin as additional columns.
* **sessionate** (optional): Include this parameter to re-do session labeling according to the time threshold specified by the *minthreshold* parameter. Sessions will be labelled from 0 and increase one everytime the next activity is performed after previous_activity_time + minthreshold.
* **minthreshold** (optional,default=90): Threshold time in minutes between sessions. If not provided and sessionate parameter is provided, default value for the threshold is 90 minutes.
* **queryArchive** (optional, [yes,no],default=no): To indicate if archive should be queried. If your study is not very old, you can ignore this parameter.

### GetSequences Service

This service is similar to RawActivity (same parameters, plus some extra ones). It can sequence the data by session, by topic, and include all activity, only questions, only examples, or questions and examples. It  generates a file with each row corresponding to either each action (parameter extended provided) where actions has been labeled to sequences, or the whole sequence.  

#### GetSequences Parameters

* **grp** (required): To specify a group or a set of groups to retrieve the data associated with. Use comma to separate multiple groups (without space), e.g. grp=group1,group2
* **mode** (default=0): 0: by session, 1: by question (use only with include=1), 2: by session/question  (use only with include=1), 3: by topic
* **include** (default=0): 0: include all, 1: only questions, 2: only examples (including Animated Ex), 3: questions and examples (including Animated Ex)
* **extended** (optional): if provided, the output will contain each user activity in each row with the following columns:  
*user|group|session|appid|applabel|activityname|targetname|parentname|topicname|result| datestring|durationseconds|timelabel|sequenceid|groupingid|actionno|label|labelreadable*
If not provided, the output contains a sequence per row with the following columns:  
*user|group|sequenceid|groupingid|size|totaltime|sequence|sequencereadable*
* **pexspam** (optional): if provided, the output will contain a file with 3 parts:

  + (1) Label map in the following format: label \t one_letter_label \t readable_label
  + (2) sequences for mining:  
        ACDA  
        1111
  + (3) All sequences with id for further pattern counting
* **half** ([1,2], optional): Returns only the first/second half of sequences for each student. If the number of sequences is odd, the first half always contains 1 more activity compared to the second half.
* **labelmap** (optional): If provided, the label map for pexspam output is added at the top of the file.
* **header** (optional, [yes,no], default=no): If yes, the names of columns are included to the response, e.g. header=yes
* **delimiter** (optional, default=,):The delimiter to separate columns, e.g. delimiter=,
* **filename** (optional, default=group_ids_sequences.txt): Name of the output file.  
* **usr**: To specify a user or a set of users to retrieve the data associated with. The output will keep the given user order, e.g. usr=usr1,usr2. Given users should be registered in the specified groups.
* **fromDate** (optional): Restrict data with date > *fromDate*. Format is YYYY-MM-DD HH:SS:ss, e.g. fromDate=2020-06-01
* **toDate** (optional): Restrict data with date < *toDate*. Format is YYYY-MM-DD HH:SS:ss, e.g. toDate=2020-06-01
* **svc** (optional, [yes,no], default=no): To include SVC field stored with user activities to the output. It varies based on the content type.
* **removeUsr** (optional): Specify user(s) to be removed, comma-separated, e.g. removeUsr=usr1,usr2. Users listed in Common.non_students are automatically removed from the output.
* **allparameters** (optional, [yes,no], default=no): To include allparameters field stored with user activities to the output.
* **queryArchive** (optional, [yes,no],default=no): To indicate if archive should be queried. If your study is not very old, you can ignore this parameter.

### FixTracking Service

This service generate a file with SQL update scripts for correcting bad tracked data (userid=1, groupid=2, appid=1, activityid=1). These bad tracked rows are the result of non matching activity or user or group names provided to cbum. The most common reason is the records of new users before restarting cbum. It generates update scripts for all the records in the user activity table. No group/user restriction is currently available.

#### FixTracking Parameters

* **archive** (optional): If provided, SQL update scripts are generated for the archive table.  

## Old Service Descriptions

The old service description is available at <https://docs.google.com/document/d/12UfF9YE0zQtoxniRKohiOotuuhXcp9zAYLZ51-Ia1eI>  
