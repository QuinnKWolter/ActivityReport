import { LoggedActivity } from './LoggedActivity';
import { Sequence } from './Sequence';
import { AppId, isContent, MIN_MOUSEOVER_TIME } from '../common';
import { SessionActivity } from './SessionActivity';

export const EARLY_ATT_TH = 15;

export class User {
  userId: number;
  userLogin: string;
  activity: LoggedActivity[] = [];
  sequences: Sequence[] | null = null;
  summary: Record<string, number> = {};

  constructor(userId: number, userLogin: string) {
    this.userId = userId;
    this.userLogin = userLogin;
    this.activity = [];
    this.sequences = null;
  }

  addLoggedActivity(act: LoggedActivity): void {
    this.activity.push(act);
  }

  generateSessionIds(thresholdMins: number): void {
    if (!this.activity || this.activity.length === 0) return;

    let sessionId = 0;
    let previousAct = this.activity[0];
    previousAct.session = sessionId.toString();

    for (let i = 1; i < this.activity.length; i++) {
      const currentAct = this.activity[i];
      const diff = currentAct.date.getTime() - previousAct.date.getTime();
      if (diff > thresholdMins * 60 * 1000) {
        sessionId++;
      }
      currentAct.session = sessionId.toString();
      previousAct = currentAct;
    }
  }

  computeActivityTimes(): void {
    if (!this.activity || this.activity.length === 0) return;

    let previousAct = this.activity[0];
    previousAct.time = 0.0;

    for (let i = 1; i < this.activity.length; i++) {
      const currentAct = this.activity[i];

      if (currentAct.appId === AppId.MASTERY_GRIDS && currentAct.activityName === 'activity-done') {
        continue;
      }

      let duration = 0.0;
      if (currentAct.session === previousAct.session) {
        if (currentAct.appId === AppId.WEBEX) {
          if (i + 1 < this.activity.length) {
            const nextAct = this.activity[i + 1];
            if (currentAct.session === nextAct.session && 
                (nextAct.appId === AppId.WEBEX || nextAct.appId === AppId.MASTERY_GRIDS)) {
              duration = (nextAct.date.getTime() - currentAct.date.getTime()) / 1000.0;
            }
          }
        } else {
          duration = (currentAct.date.getTime() - previousAct.date.getTime()) / 1000.0;
        }
      }

      currentAct.time = duration;
      previousAct = currentAct;
    }
  }

  computeAttemptNo(): void {
    if (!this.activity || this.activity.length === 0) return;

    const activityAttemptNoMap = new Map<number, number>();

    for (const act of this.activity) {
      if (act.logType === 'UM') {
        const current = activityAttemptNoMap.get(act.activityId) || 0;
        activityAttemptNoMap.set(act.activityId, current + 1);
        act.attemptNo = activityAttemptNoMap.get(act.activityId)!;
      }
    }
  }

  getSummary(timeBins?: number[] | null): void {
    const sessions = new Map<string, SessionActivity>();
    const topics = new Set<string>();
    const pcex_topics = new Set<string>();
    const parson_topics = new Set<string>();
    const sqlknot_topics = new Set<string>();
    const questions = new Set<string>();
    const success_questions = new Set<string>();
    const examples = new Set<string>();
    const animated_examples = new Set<string>();
    const parsons = new Set<string>();
    const parsonsTopicCorrectAttemptMap = new Map<number, Map<number, number>>();
    const parsonsCorrectAttemptMap = new Map<number, number>();
    const success_parsons = new Set<string>();
    const lesslet = new Set<string>();
    const lesslet_description = new Set<string>();
    const lesslet_example = new Set<string>();
    const success_lesslet = new Set<string>();
    const pcrs = new Set<string>();
    const success_pcrs = new Set<string>();
    const sqltutor = new Set<string>();
    const success_sqltutor = new Set<string>();
    const dbqa = new Set<string>();
    const completed_dbqa = new Set<string>();
    const pcrsCorrectAttemptMap = new Map<number, number>();
    const pcex_ex = new Set<string>();
    const pcex_ch = new Set<string>();
    const pcexTopicCorrectAttemptMap = new Map<number, Map<number, number>>();
    const pcexCorrectAttemptMap = new Map<number, number>();
    const success_pcex = new Set<string>();
    
    const time_summary: Record<string, number> = {
      total: 0.0,
      quizjet: 0.0,
      sqlknot: 0.0,
      sqllab: 0.0,
      webex: 0.0,
      animated_example: 0.0,
      parsons: 0.0,
      lesslet: 0.0,
      lesslet_description: 0.0,
      lesslet_example: 0.0,
      lesslet_test: 0.0,
      pcrs: 0.0,
      pcrs_first_attempt: 0.0,
      pcrs_second_attempt: 0.0,
      pcrs_third_attempt: 0.0,
      sqltutor: 0.0,
      dbqa: 0.0,
      pcex_ex: 0.0,
      pcex_ex_lines: 0.0,
      pcex_ch: 0.0,
      pcex_ch_first_attempt: 0.0,
      pcex_ch_second_attempt: 0.0,
      pcex_ch_third_attempt: 0.0,
      pcex_control_explanations_shown: 0.0,
      pcex_control_explanations_not_shown: 0.0,
      mastery_grid: 0.0,
    };
    
    const mg_summary: Record<string, number> = {
      mg_total_loads: 0,
      mg_topic_cell_clicks: 0,
      mg_topic_cell_clicks_me: 0,
      mg_topic_cell_clicks_grp: 0,
      mg_topic_cell_clicks_mevsgrp: 0,
      mg_activity_cell_clicks: 0,
      mg_activity_cell_clicks_me: 0,
      mg_activity_cell_clicks_grp: 0,
      mg_activity_cell_clicks_mevsgrp: 0,
      mg_load_rec: 0,
      mg_load_original: 0,
      mg_difficulty_feedback: 0,
      mg_change_comparison_mode: 0,
      mg_change_group: 0,
      mg_change_resource_set: 0,
      mg_load_others: 0,
      mg_grid_activity_cell_mouseover: 0,
      mg_grid_topic_cell_mouseover: 0,
      mg_cm_concept_mouseover: 0,
    };
    
    let attempts = 0;
    let correct_attempts = 0;
    const questionsCorrectAttemptMap = new Map<number, number>();
    let parsons_attempts = 0;
    let parsons_correct_attempts = 0;
    let lesslet_attempts = 0;
    let lesslet_correct_attempts = 0;
    let lesslet_description_seen = 0;
    let lesslet_examples_seen = 0;
    let pcrs_attempts = 0;
    let pcrs_correct_attempts = 0;
    let sqltutor_attempts = 0;
    let sqltutor_correct_attempts = 0;
    let pcex_ch_attempts = 0;
    let pcex_ch_correct_attempts = 0;
    let pcex_completed_set = 0; // TODO: Implement PCEX set completion tracking
    let example_lines = 0;
    let animated_example_lines = 0;
    let dbqa_steps = 0;
    let dbqa_final_steps = 0;
    let sql_knot_attempts = 0;
    let sql_lab_attempts = 0;
    const sqlKnotCorrectAttemptMap = new Map<number, number>();
    
    let max_webex_time = 0;
    let max_animated_time = 0;
    let max_lesslet_time = 0;
    let max_sql_knot_time = 0;
    let max_sql_lab_time = 0;

    if (!this.activity || this.activity.length === 0) {
      this.initializeEmptySummary();
      return;
    }

    // Process all activities
    for (const a of this.activity) {
      // Initialize session if needed
      if (!sessions.has(a.session)) {
        sessions.set(a.session, new SessionActivity());
      }
      const sessionActivity = sessions.get(a.session)!;
      
      time_summary.total += a.time;
      sessionActivity.addTime(a.time);
      
      // QUIZJET, QUIZPET, SQLKNOT
      if (a.appId === AppId.QUIZJET || a.appId === AppId.QUIZPET || a.appId === AppId.SQLKNOT) {
        if (a.parentName) questions.add(a.parentName);
        
        if (a.appId === AppId.QUIZJET || a.appId === AppId.QUIZPET) {
          time_summary.quizjet += a.time;
          
          if (a.result === 1.0 || a.result === 1) {
            if (a.topicName) topics.add(a.topicName);
            if (a.parentName) success_questions.add(a.parentName);
            correct_attempts++;
            
            const current = questionsCorrectAttemptMap.get(a.attemptNo) || 0;
            questionsCorrectAttemptMap.set(a.attemptNo, current + 1);
          }
        } else {
          if (a.topicName === 'sqllab') {
            time_summary.sqllab += a.time;
            sql_lab_attempts++;
            if (a.time > max_sql_lab_time) max_sql_lab_time = a.time;
          } else {
            time_summary.sqlknot += a.time;
            sql_knot_attempts++;
            if (a.time > max_sql_knot_time) max_sql_knot_time = a.time;
            
            if (a.result === 1.0 || a.result === 1) {
              if (a.topicName) {
                topics.add(a.topicName);
                sqlknot_topics.add(a.topicName);
              }
              if (a.parentName) success_questions.add(a.parentName);
              correct_attempts++;
              
              const current = sqlKnotCorrectAttemptMap.get(a.attemptNo) || 0;
              sqlKnotCorrectAttemptMap.set(a.attemptNo, current + 1);
            }
          }
        }
        attempts++;
        if (a.parentName) sessionActivity.addQuestion(a.parentName);
      }
      // PARSONS
      else if (a.appId === AppId.PARSONS) {
        if (a.parentName) parsons.add(a.parentName);
        time_summary.parsons += a.time;
        parsons_attempts++;
        
        if (a.result === 1 || a.result === 1.0) {
          if (a.topicName) {
            topics.add(a.topicName);
            parson_topics.add(a.topicName);
          }
          if (a.parentName) success_parsons.add(a.parentName);
          parsons_correct_attempts++;
          
          const current = parsonsCorrectAttemptMap.get(a.attemptNo) || 0;
          parsonsCorrectAttemptMap.set(a.attemptNo, current + 1);
          
          if (!parsonsTopicCorrectAttemptMap.has(a.topicOrder)) {
            parsonsTopicCorrectAttemptMap.set(a.topicOrder, new Map());
          }
          const topicMap = parsonsTopicCorrectAttemptMap.get(a.topicOrder)!;
          const topicCurrent = topicMap.get(a.attemptNo) || 0;
          topicMap.set(a.attemptNo, topicCurrent + 1);
        }
        if (a.parentName) sessionActivity.addParson(a.parentName);
      }
      // SQLTUTOR
      else if (a.appId === AppId.SQLTUTOR) {
        if (a.activityName) sqltutor.add(a.activityName);
        sqltutor_attempts++;
        time_summary.sqltutor += a.time;
        
        if (a.result === 1 || a.result === 1.0) {
          if (a.topicName) topics.add(a.topicName);
          if (a.activityName) success_sqltutor.add(a.activityName);
          sqltutor_correct_attempts++;
        }
        if (a.activityName) sessionActivity.addQuestion(a.activityName);
      }
      // LESSLET
      else if (a.appId === AppId.LESSLET) {
        if (a.parentName) lesslet.add(a.parentName);
        if (a.time > max_lesslet_time) max_lesslet_time = a.time;
        
        if (a.activityName && a.activityName.endsWith('description')) {
          lesslet_description_seen++;
          if (a.activityName) lesslet_description.add(a.activityName);
          time_summary.lesslet_description += a.time;
        } else if (a.activityName && a.activityName.endsWith('example')) {
          lesslet_examples_seen++;
          if (a.activityName) lesslet_example.add(a.activityName);
          time_summary.lesslet_example += a.time;
        } else if (a.activityName && a.activityName.endsWith('test')) {
          lesslet_attempts++;
          time_summary.lesslet_test += a.time;
        }
        
        time_summary.lesslet += a.time;
        if (a.result === 1 || a.result === 1.0) {
          if (a.parentName) success_lesslet.add(a.parentName);
          lesslet_correct_attempts++;
        }
        if (a.parentName) sessionActivity.addLesslet(a.parentName);
      }
      // PCRS
      else if (a.appId === AppId.PCRS) {
        if (a.parentName) pcrs.add(a.parentName);
        time_summary.pcrs += a.time;
        pcrs_attempts++;
        
        if (a.result === 1 || a.result === 1.0) {
          if (a.topicName) topics.add(a.topicName);
          if (a.parentName) success_pcrs.add(a.parentName);
          pcrs_correct_attempts++;
          
          const current = pcrsCorrectAttemptMap.get(a.attemptNo) || 0;
          pcrsCorrectAttemptMap.set(a.attemptNo, current + 1);
          
          if (a.attemptNo === 1) time_summary.pcrs_first_attempt += a.time;
          else if (a.attemptNo === 2) time_summary.pcrs_second_attempt += a.time;
          else if (a.attemptNo === 3) time_summary.pcrs_third_attempt += a.time;
        }
        if (a.parentName) sessionActivity.addQuestion(a.parentName);
      }
      // PCEX_EXAMPLE
      else if (a.appId === AppId.PCEX_EXAMPLE) {
        if (a.parentName) pcex_ex.add(a.parentName);
        
        if (a.logType === 'UM') {
          time_summary.pcex_ex_lines += a.time;
          example_lines++;
          if (a.parentName) sessionActivity.addExample(a.parentName);
        } else {
          time_summary.pcex_ex += a.time;
          
          if (a.logType === 'PCEX_CONTROL') {
            const explanationShown = parseInt(a.svc || '0') === 1;
            if (explanationShown) {
              time_summary.pcex_control_explanations_shown += a.time;
            } else {
              time_summary.pcex_control_explanations_not_shown += a.time;
            }
          }
        }
      }
      // PCEX_CHALLENGE
      else if (a.appId === AppId.PCEX_CHALLENGE) {
        if (a.parentName) pcex_ch.add(a.parentName);
        time_summary.pcex_ch += a.time;
        
        if (a.logType === 'UM') {
          pcex_ch_attempts++;
          if (a.parentName) sessionActivity.addChallenge(a.parentName);
        } else if (a.logType === 'PCEX_CONTROL') {
          const explanationShown = parseInt(a.svc || '0') === 1;
          if (explanationShown) {
            time_summary.pcex_control_explanations_shown += a.time;
          } else {
            time_summary.pcex_control_explanations_not_shown += a.time;
          }
          
          if (a.topicName) {
            topics.add(a.topicName);
            pcex_topics.add(a.topicName);
          }
        }
        
        if (a.result === 1 || a.result === 1.0) {
          if (a.topicName) {
            topics.add(a.topicName);
            pcex_topics.add(a.topicName);
          }
          if (a.parentName) success_pcex.add(a.parentName);
          pcex_ch_correct_attempts++;
          
          const current = pcexCorrectAttemptMap.get(a.attemptNo) || 0;
          pcexCorrectAttemptMap.set(a.attemptNo, current + 1);
          
          if (!pcexTopicCorrectAttemptMap.has(a.topicOrder)) {
            pcexTopicCorrectAttemptMap.set(a.topicOrder, new Map());
          }
          const topicMap = pcexTopicCorrectAttemptMap.get(a.topicOrder)!;
          const topicCurrent = topicMap.get(a.attemptNo) || 0;
          topicMap.set(a.attemptNo, topicCurrent + 1);
          
          if (a.attemptNo === 1) time_summary.pcex_ch_first_attempt += a.time;
          else if (a.attemptNo === 2) time_summary.pcex_ch_second_attempt += a.time;
          else if (a.attemptNo === 3) time_summary.pcex_ch_third_attempt += a.time;
        }
      }
      // WEBEX
      else if (a.appId === AppId.WEBEX) {
        if (a.parentName) examples.add(a.parentName);
        if (a.time > max_webex_time) max_webex_time = a.time;
        time_summary.webex += a.time;
        example_lines++;
        if (a.parentName) sessionActivity.addExample(a.parentName);
        if (a.topicName) topics.add(a.topicName);
      }
      // ANIMATED_EXAMPLE
      else if (a.appId === AppId.ANIMATED_EXAMPLE) {
        if (a.parentName) animated_examples.add(a.parentName);
        if (a.time > max_animated_time) max_animated_time = a.time;
        time_summary.animated_example += a.time;
        animated_example_lines++;
        if (a.parentName) sessionActivity.addAnimation(a.parentName);
        if (a.topicName) topics.add(a.topicName);
      }
      // DBQA
      else if (a.appId === AppId.DBQA) {
        if (a.parentName) dbqa.add(a.parentName);
        time_summary.dbqa += a.time;
        dbqa_steps++;
        if (a.result === 1 || a.result === 1.0) {
          if (a.parentName) completed_dbqa.add(a.parentName);
          dbqa_final_steps++;
        }
        if (a.parentName) sessionActivity.addQuestion(a.parentName);
      }
      // MASTERY_GRIDS
      else if (a.appId === AppId.MASTERY_GRIDS) {
        time_summary.mastery_grid += a.time;
        const action = a.activityName;
        const grid_name = a.parentName || '';
        
        if (action && action.includes('data-load-end')) {
          mg_summary.mg_total_loads++;
        } else if (action && action.includes('grid-topic-cell-select')) {
          mg_summary.mg_topic_cell_clicks++;
          if (grid_name.includes('me vs grp')) {
            mg_summary.mg_topic_cell_clicks_mevsgrp++;
          } else if (grid_name.includes('me')) {
            mg_summary.mg_topic_cell_clicks_me++;
          } else if (grid_name.includes('grp')) {
            mg_summary.mg_topic_cell_clicks_grp++;
          }
        } else if (action && action.includes('grid-activity-cell-select')) {
          mg_summary.mg_activity_cell_clicks++;
          if (grid_name.includes('me vs grp')) {
            mg_summary.mg_activity_cell_clicks_mevsgrp++;
          } else if (grid_name.includes('me')) {
            mg_summary.mg_activity_cell_clicks_me++;
          } else if (grid_name.includes('grp')) {
            mg_summary.mg_activity_cell_clicks_grp++;
          }
        } else if (action && action.includes('activity-load-recommended')) {
          mg_summary.mg_load_rec++;
        } else if (action && action.includes('activity-load-original')) {
          mg_summary.mg_load_original++;
        } else if (action && action.includes('activity-feedback-set-difficulty')) {
          mg_summary.mg_difficulty_feedback++;
        } else if (action && action.includes('comparison-mode-set')) {
          mg_summary.mg_change_comparison_mode++;
        } else if (action && action.includes('group-set')) {
          mg_summary.mg_change_group++;
        } else if (action && action.includes('resource-set')) {
          mg_summary.mg_change_resource_set++;
        } else if (action && action.includes('load-others-list')) {
          mg_summary.mg_load_others++;
        } else if (action && action.includes('grid-activity-cell-mouseover') && a.time > MIN_MOUSEOVER_TIME) {
          mg_summary.mg_grid_activity_cell_mouseover++;
        } else if (action && action.includes('grid-topic-cell-mouseover') && a.time > MIN_MOUSEOVER_TIME) {
          mg_summary.mg_grid_topic_cell_mouseover++;
        } else if (action && action.includes('cm-concept-mouseover') && a.time > MIN_MOUSEOVER_TIME) {
          mg_summary.mg_cm_concept_mouseover++;
        }
      }
    }
    
    // Compute medians
    const exampleMedian = this.computeMedianTimeForApp(AppId.PCEX_EXAMPLE);
    const challengeMedian = this.computeMedianTimeForApp(AppId.PCEX_CHALLENGE);
    const parsonMedian = this.computeMedianTimeForApp(AppId.PARSONS);
    
    // Compute first/second half topic-based success
    const pcexFirstSecondHalf = this.computeFirstSecondHalfTopicBasedSuccess(pcexTopicCorrectAttemptMap);
    const parsonsFirstSecondHalf = this.computeFirstSecondHalfTopicBasedSuccess(parsonsTopicCorrectAttemptMap);
    
    // Compute session medians
    const medianSessionAct = this.computeMedianOfSession(sessions, true);
    const medianSessionTime = this.computeMedianOfSession(sessions, false);
    const medianSessionSelfAssesment = this.computeMedianOfSessionSelfAssesment(sessions);
    const medianSessionExamples = this.computeMedianOfSessionExampleLines(sessions);
    
    // Count opened/not attempted
    const openedAttOpenedNotAtt = this.countOpenedNotAttempted(EARLY_ATT_TH);
    
    // Build summary
    this.summary = {
      sessions_dist: sessions.size,
      median_sessions_act: medianSessionAct,
      median_sessions_time: medianSessionTime,
      median_sessions_self_assesment: medianSessionSelfAssesment,
      median_sessions_example_lines: medianSessionExamples,
      topics_covered: topics.size,
      parsons_topics_covered: parson_topics.size,
      pcex_topics_covered: pcex_topics.size,
      sqlknot_topics_covered: sqlknot_topics.size,
      question_attempts: attempts,
      question_attempts_success: correct_attempts,
      questions_dist: questions.size,
      questions_dist_success: success_questions.size,
      questions_sucess_first_attempt: questionsCorrectAttemptMap.get(1) || 0,
      questions_sucess_second_attempt: questionsCorrectAttemptMap.get(2) || 0,
      questions_sucess_third_attempt: questionsCorrectAttemptMap.get(3) || 0,
      sql_knot_attempts: sql_knot_attempts,
      sql_lab_attempts: sql_lab_attempts,
      sqlknot_sucess_first_attempt: sqlKnotCorrectAttemptMap.get(1) || 0,
      sqlknot_sucess_second_attempt: sqlKnotCorrectAttemptMap.get(2) || 0,
      sqlknot_sucess_third_attempt: sqlKnotCorrectAttemptMap.get(3) || 0,
      examples_dist: examples.size,
      example_lines_actions: example_lines,
      animated_examples_dist: animated_examples.size,
      animated_example_lines_actions: animated_example_lines,
      parsons_attempts: parsons_attempts,
      parsons_attempts_success: parsons_correct_attempts,
      parsons_dist: parsons.size,
      parsons_dist_success: success_parsons.size,
      parsons_sucess_first_attempt: parsonsCorrectAttemptMap.get(1) || 0,
      parsons_sucess_second_attempt: parsonsCorrectAttemptMap.get(2) || 0,
      parsons_sucess_third_attempt: parsonsCorrectAttemptMap.get(3) || 0,
      parsons_sucess_first_attempt_first_half: parsonsFirstSecondHalf[0].get(1) || 0,
      parsons_sucess_second_attempt_first_half: parsonsFirstSecondHalf[0].get(2) || 0,
      parsons_sucess_third_attempt_first_half: parsonsFirstSecondHalf[0].get(3) || 0,
      parsons_sucess_first_attempt_second_half: parsonsFirstSecondHalf[1].get(1) || 0,
      parsons_sucess_second_attempt_second_half: parsonsFirstSecondHalf[1].get(2) || 0,
      parsons_sucess_third_attempt_second_half: parsonsFirstSecondHalf[1].get(3) || 0,
      lesslet_attempts: lesslet_attempts,
      lesslet_attempts_success: lesslet_correct_attempts,
      lesslet_dist: lesslet.size,
      lesslet_dist_success: success_lesslet.size,
      lesslet_description_seen: lesslet_description_seen,
      lesslet_dist_description_seen: lesslet_description.size,
      lesslet_example_seen: lesslet_examples_seen,
      lesslet_dist_example_seen: lesslet_example.size,
      pcrs_attempts: pcrs_attempts,
      pcrs_attempts_success: pcrs_correct_attempts,
      pcrs_dist: pcrs.size,
      pcrs_dist_success: success_pcrs.size,
      pcrs_success_first_attempt: pcrsCorrectAttemptMap.get(1) || 0,
      pcrs_success_second_attempt: pcrsCorrectAttemptMap.get(2) || 0,
      pcrs_success_third_attempt: pcrsCorrectAttemptMap.get(3) || 0,
      sqltutor_attempts: sqltutor_attempts,
      sqltutor_attempts_success: sqltutor_correct_attempts,
      sqltutor_dist: sqltutor.size,
      sqltutor_dist_success: success_sqltutor.size,
      dbqa_steps: dbqa_steps,
      dbqa_final_steps: dbqa_final_steps,
      dbqa_dist: dbqa.size,
      dbqa_dist_completed: completed_dbqa.size,
      pcex_completed_set: pcex_completed_set,
      pcex_ex_dist_seen: pcex_ex.size,
      pcex_ch_attempts: pcex_ch_attempts,
      pcex_ch_attempts_success: pcex_ch_correct_attempts,
      pcex_ch_dist: pcex_ch.size,
      pcex_ch_dist_success: success_pcex.size,
      pcex_success_first_attempt: pcexCorrectAttemptMap.get(1) || 0,
      pcex_success_second_attempt: pcexCorrectAttemptMap.get(2) || 0,
      pcex_success_third_attempt: pcexCorrectAttemptMap.get(3) || 0,
      pcex_sucess_first_attempt_first_half: pcexFirstSecondHalf[0].get(1) || 0,
      pcex_sucess_second_attempt_first_half: pcexFirstSecondHalf[0].get(2) || 0,
      pcex_sucess_third_attempt_first_half: pcexFirstSecondHalf[0].get(3) || 0,
      pcex_sucess_first_attempt_second_half: pcexFirstSecondHalf[1].get(1) || 0,
      pcex_sucess_second_attempt_second_half: pcexFirstSecondHalf[1].get(2) || 0,
      pcex_sucess_third_attempt_second_half: pcexFirstSecondHalf[1].get(3) || 0,
      durationseconds_total: time_summary.total,
      durationseconds_quizjet: time_summary.quizjet,
      durationseconds_sqlknot: time_summary.sqlknot,
      durationseconds_sqllab: time_summary.sqllab,
      durationseconds_webex: time_summary.webex,
      durationseconds_animated_example: time_summary.animated_example,
      durationseconds_parsons: time_summary.parsons,
      durationseconds_parsons_median: parsonMedian,
      durationseconds_lesslet: time_summary.lesslet,
      durationseconds_lesslet_description: time_summary.lesslet_description,
      durationseconds_lesslet_example: time_summary.lesslet_example,
      durationseconds_lesslet_test: time_summary.lesslet_test,
      durationseconds_pcrs: time_summary.pcrs,
      durationseconds_pcrs_first_attempt: time_summary.pcrs_first_attempt,
      durationseconds_pcrs_second_attempt: time_summary.pcrs_second_attempt,
      durationseconds_pcrs_third_attempt: time_summary.pcrs_third_attempt,
      durationseconds_sqltutor: time_summary.sqltutor,
      durationseconds_dbqa: time_summary.dbqa,
      durationseconds_pcex_ex: time_summary.pcex_ex,
      durationseconds_pcex_ex_median: exampleMedian,
      durationseconds_pcex_ex_lines: time_summary.pcex_ex_lines,
      durationseconds_pcex_ch: time_summary.pcex_ch,
      durationseconds_pcex_ch_median: challengeMedian,
      durationseconds_pcex_ch_first_attempt: time_summary.pcex_ch_first_attempt,
      durationseconds_pcex_ch_second_attempt: time_summary.pcex_ch_second_attempt,
      durationseconds_pcex_ch_third_attempt: time_summary.pcex_ch_third_attempt,
      durationseconds_pcex_control_explanations: time_summary.pcex_control_explanations_shown,
      durationseconds_pcex_control_no_explanations: time_summary.pcex_control_explanations_not_shown,
      durationseconds_mastery_grid: time_summary.mastery_grid,
      mg_total_loads: mg_summary.mg_total_loads,
      mg_topic_cell_clicks: mg_summary.mg_topic_cell_clicks,
      mg_topic_cell_clicks_me: mg_summary.mg_topic_cell_clicks_me,
      mg_topic_cell_clicks_grp: mg_summary.mg_topic_cell_clicks_grp,
      mg_topic_cell_clicks_mevsgrp: mg_summary.mg_topic_cell_clicks_mevsgrp,
      mg_activity_cell_clicks: mg_summary.mg_activity_cell_clicks,
      mg_activity_cell_clicks_me: mg_summary.mg_activity_cell_clicks_me,
      mg_activity_cell_clicks_grp: mg_summary.mg_activity_cell_clicks_grp,
      mg_activity_cell_clicks_mevsgrp: mg_summary.mg_activity_cell_clicks_mevsgrp,
      mg_load_rec: mg_summary.mg_load_rec,
      mg_load_original: mg_summary.mg_load_original,
      mg_difficulty_feedback: mg_summary.mg_difficulty_feedback,
      mg_change_comparison_mode: mg_summary.mg_change_comparison_mode,
      mg_change_group: mg_summary.mg_change_group,
      mg_change_resource_set: mg_summary.mg_change_resource_set,
      mg_load_others: mg_summary.mg_load_others,
      mg_grid_activity_cell_mouseover: mg_summary.mg_grid_activity_cell_mouseover,
      mg_grid_topic_cell_mouseover: mg_summary.mg_grid_topic_cell_mouseover,
      mg_cm_concept_mouseover: mg_summary.mg_cm_concept_mouseover,
      mg_act_open_not_attempted: openedAttOpenedNotAtt[0],
      mg_act_open_and_attempted: openedAttOpenedNotAtt[1],
      mg_act_open_not_attempted_difficulty: openedAttOpenedNotAtt[2],
      mg_act_open_and_attempted_difficulty: openedAttOpenedNotAtt[3],
      [`mg_act_open_not_attempted_difficulty_early${EARLY_ATT_TH}`]: openedAttOpenedNotAtt[4],
      [`mg_act_open_and_attempted_difficulty_early${EARLY_ATT_TH}`]: openedAttOpenedNotAtt[5],
    };
    
    // Add time bin metrics if provided
    if (timeBins && timeBins.length > 0) {
      const byTimeBins = this.countActivityByTimeBins(timeBins);
      for (let j = 0; j < byTimeBins.length; j++) {
        this.summary[`mg_bin${j}_act_opened_att`] = byTimeBins[j][0];
        this.summary[`mg_bin${j}_act_opened_notatt`] = byTimeBins[j][1];
        this.summary[`mg_bin${j}_act_interface`] = byTimeBins[j][2];
        this.summary[`mg_bin${j}_time`] = byTimeBins[j][3];
        this.summary[`mg_bin${j}_time_interface`] = byTimeBins[j][4];
        this.summary[`mg_bin${j}_act_opened_att_DIFF`] = byTimeBins[j][5];
        this.summary[`mg_bin${j}_act_opened_notatt_DIFF`] = byTimeBins[j][6];
      }
    }
  }

  private initializeEmptySummary(): void {
    // Initialize all summary fields to 0 or -1 as appropriate
    this.summary = {
      sessions_dist: 0,
      median_sessions_act: -1,
      median_sessions_time: -1,
      median_sessions_self_assesment: -1,
      median_sessions_example_lines: -1,
      topics_covered: 0,
      parsons_topics_covered: 0,
      pcex_topics_covered: 0,
      sqlknot_topics_covered: 0,
      question_attempts: 0,
      question_attempts_success: 0,
      questions_dist: 0,
      questions_dist_success: 0,
      questions_sucess_first_attempt: 0,
      questions_sucess_second_attempt: 0,
      questions_sucess_third_attempt: 0,
      sql_knot_attempts: 0,
      sql_lab_attempts: 0,
      sqlknot_sucess_first_attempt: 0,
      sqlknot_sucess_second_attempt: 0,
      sqlknot_sucess_third_attempt: 0,
      examples_dist: 0,
      example_lines_actions: 0,
      animated_examples_dist: 0,
      animated_example_lines_actions: 0,
      parsons_attempts: 0,
      parsons_attempts_success: 0,
      parsons_dist: 0,
      parsons_dist_success: 0,
      parsons_sucess_first_attempt: 0,
      parsons_sucess_second_attempt: 0,
      parsons_sucess_third_attempt: 0,
      parsons_sucess_first_attempt_first_half: 0,
      parsons_sucess_second_attempt_first_half: 0,
      parsons_sucess_third_attempt_first_half: 0,
      parsons_sucess_first_attempt_second_half: 0,
      parsons_sucess_second_attempt_second_half: 0,
      parsons_sucess_third_attempt_second_half: 0,
      lesslet_attempts: 0,
      lesslet_attempts_success: 0,
      lesslet_dist: 0,
      lesslet_dist_success: 0,
      lesslet_description_seen: 0,
      lesslet_dist_description_seen: 0,
      lesslet_example_seen: 0,
      lesslet_dist_example_seen: 0,
      pcrs_attempts: 0,
      pcrs_attempts_success: 0,
      pcrs_dist: 0,
      pcrs_dist_success: 0,
      pcrs_success_first_attempt: 0,
      pcrs_success_second_attempt: 0,
      pcrs_success_third_attempt: 0,
      sqltutor_attempts: 0,
      sqltutor_attempts_success: 0,
      sqltutor_dist: 0,
      sqltutor_dist_success: 0,
      dbqa_steps: 0,
      dbqa_final_steps: 0,
      dbqa_dist: 0,
      dbqa_dist_completed: 0,
      pcex_completed_set: 0,
      pcex_ex_dist_seen: 0,
      pcex_ch_attempts: 0,
      pcex_ch_attempts_success: 0,
      pcex_ch_dist: 0,
      pcex_ch_dist_success: 0,
      pcex_success_first_attempt: 0,
      pcex_success_second_attempt: 0,
      pcex_success_third_attempt: 0,
      pcex_sucess_first_attempt_first_half: 0,
      pcex_sucess_second_attempt_first_half: 0,
      pcex_sucess_third_attempt_first_half: 0,
      pcex_sucess_first_attempt_second_half: 0,
      pcex_sucess_second_attempt_second_half: 0,
      pcex_sucess_third_attempt_second_half: 0,
      durationseconds_total: 0,
      durationseconds_quizjet: 0,
      durationseconds_sqlknot: 0,
      durationseconds_sqllab: 0,
      durationseconds_webex: 0,
      durationseconds_animated_example: 0,
      durationseconds_parsons: 0,
      durationseconds_parsons_median: 0,
      durationseconds_lesslet: 0,
      durationseconds_lesslet_description: 0,
      durationseconds_lesslet_example: 0,
      durationseconds_lesslet_test: 0,
      durationseconds_pcrs: 0,
      durationseconds_pcrs_first_attempt: 0,
      durationseconds_pcrs_second_attempt: 0,
      durationseconds_pcrs_third_attempt: 0,
      durationseconds_sqltutor: 0,
      durationseconds_dbqa: 0,
      durationseconds_pcex_ex: 0,
      durationseconds_pcex_ex_median: 0,
      durationseconds_pcex_ex_lines: 0,
      durationseconds_pcex_ch: 0,
      durationseconds_pcex_ch_median: 0,
      durationseconds_pcex_ch_first_attempt: 0,
      durationseconds_pcex_ch_second_attempt: 0,
      durationseconds_pcex_ch_third_attempt: 0,
      durationseconds_pcex_control_explanations: 0,
      durationseconds_pcex_control_no_explanations: 0,
      durationseconds_mastery_grid: 0,
      mg_total_loads: 0,
      mg_topic_cell_clicks: 0,
      mg_topic_cell_clicks_me: 0,
      mg_topic_cell_clicks_grp: 0,
      mg_topic_cell_clicks_mevsgrp: 0,
      mg_activity_cell_clicks: 0,
      mg_activity_cell_clicks_me: 0,
      mg_activity_cell_clicks_grp: 0,
      mg_activity_cell_clicks_mevsgrp: 0,
      mg_load_rec: 0,
      mg_load_original: 0,
      mg_difficulty_feedback: 0,
      mg_change_comparison_mode: 0,
      mg_change_group: 0,
      mg_change_resource_set: 0,
      mg_load_others: 0,
      mg_grid_activity_cell_mouseover: 0,
      mg_grid_topic_cell_mouseover: 0,
      mg_cm_concept_mouseover: 0,
      mg_act_open_not_attempted: 0,
      mg_act_open_and_attempted: 0,
      mg_act_open_not_attempted_difficulty: -1,
      mg_act_open_and_attempted_difficulty: -1,
      [`mg_act_open_not_attempted_difficulty_early${EARLY_ATT_TH}`]: -1,
      [`mg_act_open_and_attempted_difficulty_early${EARLY_ATT_TH}`]: -1,
    };
  }

  private computeMedianTimeForApp(appId: AppId): number {
    const activities = this.activity.filter(a => a.appId === appId);
    if (activities.length === 0) return 0;
    
    // Group by parentName and sum times
    const timeMap = new Map<string, number>();
    for (const act of activities) {
      const key = act.parentName || '';
      timeMap.set(key, (timeMap.get(key) || 0) + act.time);
    }
    
    const times = Array.from(timeMap.values()).sort((a, b) => a - b);
    if (times.length === 0) return 0;
    
    const mid = Math.floor(times.length / 2);
    if (times.length % 2 === 0) {
      return (times[mid - 1] + times[mid]) / 2;
    }
    return times[mid];
  }

  private computeFirstSecondHalfTopicBasedSuccess(
    topicCorrectAttemptMap: Map<number, Map<number, number>>
  ): Map<number, number>[] {
    const firstHalf = new Map<number, number>();
    const secondHalf = new Map<number, number>();
    
    for (const [topicOrder, attemptMap] of topicCorrectAttemptMap) {
      const targetMap = topicOrder <= 5 ? firstHalf : secondHalf;
      for (const [attemptNo, count] of attemptMap) {
        targetMap.set(attemptNo, (targetMap.get(attemptNo) || 0) + count);
      }
    }
    
    return [firstHalf, secondHalf];
  }

  private computeMedianOfSession(sessions: Map<string, SessionActivity>, actOrTime: boolean): number {
    if (sessions.size === 0) return -1;
    
    const values: number[] = [];
    for (const sessionActivity of sessions.values()) {
      values.push(actOrTime ? sessionActivity.countActivity() : sessionActivity.getTime());
    }
    
    values.sort((a, b) => a - b);
    const mid = Math.floor(values.length / 2);
    
    if (values.length % 2 === 0 && mid > 0) {
      return (values[mid - 1] + values[mid]) / 2;
    }
    return values[mid];
  }

  private computeMedianOfSessionSelfAssesment(sessions: Map<string, SessionActivity>): number {
    if (sessions.size === 0) return -1;
    
    const values: number[] = [];
    for (const sessionActivity of sessions.values()) {
      values.push(sessionActivity.countSelfAssesment());
    }
    
    values.sort((a, b) => a - b);
    const mid = Math.floor(values.length / 2);
    
    if (values.length % 2 === 0 && mid > 0) {
      return (values[mid - 1] + values[mid]) / 2;
    }
    return values[mid];
  }

  private computeMedianOfSessionExampleLines(sessions: Map<string, SessionActivity>): number {
    if (sessions.size === 0) return -1;
    
    const values: number[] = [];
    for (const sessionActivity of sessions.values()) {
      values.push(sessionActivity.countExampleLines());
    }
    
    values.sort((a, b) => a - b);
    const mid = Math.floor(values.length / 2);
    
    if (values.length % 2 === 0 && mid > 0) {
      return (values[mid - 1] + values[mid]) / 2;
    }
    return values[mid];
  }

  private countOpenedNotAttempted(diffCountTh: number): number[] {
    const r = [0, 0, -1, -1, -1, -1];
    let cNotAtt = 0;
    let cAtt = 0;
    let sumDiffNotAtt = 0.0;
    let sumDiffAtt = 0.0;
    let countNotAttBfTh = 0;
    let countAttBfTh = 0;
    let sumDiffNotAttBfTh = 0.0;
    let sumDiffAttBfTh = 0.0;
    
    for (let i = 0; i < this.activity.length - 1; i++) {
      const a = this.activity[i];
      if (a.activityName && a.activityName.toLowerCase() === 'grid-activity-cell-select') {
        // Search for an attempt following the cell select
        for (let j = i + 1; j < this.activity.length; j++) {
          const b = this.activity[j];
          if (b.activityName && b.activityName.toLowerCase() === 'grid-activity-cell-select') {
            i = j - 1;
            cNotAtt++;
            if (a.difficulty >= 0.0) sumDiffNotAtt += a.difficulty;
            if (countNotAttBfTh < diffCountTh) {
              if (a.difficulty >= 0.0) sumDiffNotAttBfTh += a.difficulty;
              countNotAttBfTh++;
            }
            break;
          } else if (isContent(b.appId)) {
            if (b.activityName === a.targetName) {
              cAtt++;
              if (a.difficulty >= 0.0) sumDiffAtt += a.difficulty;
              if (countAttBfTh < diffCountTh) {
                if (a.difficulty >= 0.0) sumDiffAttBfTh += a.difficulty;
                countAttBfTh++;
              }
            } else {
              cNotAtt++;
              if (a.difficulty >= 0.0) sumDiffNotAtt += a.difficulty;
              if (countNotAttBfTh < diffCountTh) {
                if (a.difficulty >= 0.0) sumDiffNotAttBfTh += a.difficulty;
                countNotAttBfTh++;
              }
            }
            break;
          }
        }
      }
    }
    
    r[0] = cNotAtt;
    r[1] = cAtt;
    r[2] = cNotAtt > 0 ? sumDiffNotAtt / cNotAtt : -1;
    r[3] = cAtt > 0 ? sumDiffAtt / cAtt : -1;
    r[4] = countNotAttBfTh > 0 ? sumDiffNotAttBfTh / countNotAttBfTh : -1;
    r[5] = countAttBfTh > 0 ? sumDiffAttBfTh / countAttBfTh : -1;
    return r;
  }

  private countActivityByTimeBins(timeBins: number[]): number[][] {
    const r: number[][] = [];
    for (let i = 0; i <= timeBins.length; i++) {
      r.push([0, 0, 0, 0, 0, -1, -1]);
    }
    
    for (let i = 0; i < this.activity.length; i++) {
      const a = this.activity[i];
      const actTimeStamp = a.unixTimestamp;
      let bin = 0;
      
      while (bin < timeBins.length && actTimeStamp > timeBins[bin]) {
        bin++;
      }
      
      r[bin][3] += a.time;
      
      if (a.activityName && a.activityName.toLowerCase() === 'grid-activity-cell-select') {
        for (let j = i + 1; j < this.activity.length; j++) {
          const b = this.activity[j];
          if (b.activityName && b.activityName.toLowerCase() === 'grid-activity-cell-select') {
            r[bin][1] += 1;
            if (a.difficulty >= 0.0) r[bin][6] += a.difficulty;
            break;
          } else if (isContent(b.appId)) {
            if (b.activityName === a.targetName) {
              r[bin][0] += 1;
              if (a.difficulty >= 0.0) r[bin][5] += a.difficulty;
            } else {
              r[bin][1] += 1;
              if (a.difficulty >= 0.0) r[bin][6] += a.difficulty;
            }
            break;
          }
        }
      }
      
      if (a.appId === AppId.MASTERY_GRIDS) {
        if (!a.allParameters.includes('-mouseover') || a.time > MIN_MOUSEOVER_TIME) {
          r[bin][4] += a.time;
          r[bin][2] += 1;
        }
      }
    }
    
    for (let i = 0; i < r.length; i++) {
      if (r[i][0] > 0) r[i][5] = r[i][5] / r[i][0];
      else r[i][5] = -1;
      if (r[i][1] > 0) r[i][6] = r[i][6] / r[i][1];
      else r[i][6] = -1;
    }
    
    return r;
  }
}

