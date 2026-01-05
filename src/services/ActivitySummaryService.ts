import { GroupActivityService } from './GroupActivityService';
import { Um2DBInterface } from '../db/Um2DBInterface';
import { AggregateDBInterface } from '../db/AggregateDBInterface';
import { NON_STUDENTS, NON_SESSIONS } from '../common';
import { config } from '../config';
import { User, EARLY_ATT_TH } from '../models/User';

export interface ActivitySummaryParams {
  groupIds: string[];
  header: boolean;
  filename: string;
  users?: string[];
  fromDate: string;
  toDate: string;
  timebins?: number[];
  sessionate: boolean;
  minThreshold: number;
  queryArchive: boolean;
}

export class ActivitySummaryService {
  private groupActivityService: GroupActivityService;

  constructor(um2Db: Um2DBInterface, aggregateDb: AggregateDBInterface) {
    this.groupActivityService = new GroupActivityService(um2Db, aggregateDb);
  }

  async getActivitySummary(params: ActivitySummaryParams): Promise<string> {
    const nonStudents = [...NON_STUDENTS];
    const dateRange = [params.fromDate, params.toDate];
    let output = '';

    for (const groupId of params.groupIds) {
      const groupActivity = await this.groupActivityService.getGroupActivity(
        groupId,
        nonStudents,
        NON_SESSIONS,
        true,
        dateRange,
        params.queryArchive,
        params.sessionate,
        params.minThreshold,
        params.timebins || null
      );

      if (groupActivity.size === 0) {
        return 'no activity found';
      }

      if (params.header && output === '') {
        output += this.getHeader(params.timebins || null);
      }

      const users = params.users || Array.from(groupActivity.keys());
      for (const userLogin of users) {
        const user = groupActivity.get(userLogin);
        if (!user) {
          output += this.formatEmptyUser(userLogin, groupId, params.timebins || null);
        } else {
          output += this.formatUserSummary(user, groupId, params.timebins || null);
        }
      }
    }

    return output;
  }

  private getHeader(timebins?: number[] | null): string {
    const delimiter = config.delimiter;
    let header = `user${delimiter}group${delimiter}sessions_dist${delimiter}median_sessions_act${delimiter}median_sessions_time${delimiter}median_sessions_self_assesment${delimiter}median_sessions_example_lines${delimiter}topics_covered${delimiter}parsons_topics_covered${delimiter}pcex_topics_covered${delimiter}sqlknot_topics_covered${delimiter}question_attempts${delimiter}question_attempts_success${delimiter}questions_dist${delimiter}questions_dist_success${delimiter}questions_sucess_first_attempt${delimiter}questions_sucess_second_attempt${delimiter}questions_sucess_third_attempt${delimiter}sql_knot_attempts${delimiter}sql_lab_attempts${delimiter}sqlknot_success_first_attempt${delimiter}sqlknot_success_second_attempt${delimiter}sqlknot_success_third_attempt${delimiter}examples_dist${delimiter}example_lines_actions${delimiter}animated_examples_dist${delimiter}animated_example_lines_actions${delimiter}`;
    
    header += `parsons_attempts${delimiter}parsons_attempts_success${delimiter}parsons_dist${delimiter}parsons_dist_success${delimiter}parsons_success_first_attempt${delimiter}parsons_success_second_attempt${delimiter}parsons_success_third_attempt${delimiter}parsons_success_first_attempt_first_half${delimiter}parsons_success_second_attempt_first_half${delimiter}parsons_success_third_attempt_first_half${delimiter}parsons_success_first_attempt_second_half${delimiter}parsons_success_second_attempt_second_half${delimiter}parsons_success_third_attempt_second_half${delimiter}`;
    
    header += `lesslet_attempts${delimiter}lesslet_attempts_success${delimiter}lesslet_dist${delimiter}lesslet_dist_success${delimiter}lesslet_description_seen${delimiter}lesslet_dist_description_seen${delimiter}lesslet_examples_seen${delimiter}lesslet_dist_example_seen${delimiter}`;
    
    header += `pcrs_attempts${delimiter}pcrs_attempts_success${delimiter}pcrs_dist${delimiter}pcrs_dist_success${delimiter}pcrs_success_first_attempt${delimiter}pcrs_success_second_attempt${delimiter}pcrs_success_third_attempt${delimiter}`;
    
    header += `sqltutor_attempts${delimiter}sqltutor_attempts_success${delimiter}sqltutor_dist${delimiter}sqltutor_dist_success${delimiter}`;
    
    header += `dbqa_steps${delimiter}dbqa_final_steps${delimiter}dbqa_dist${delimiter}dbqa_dist_completed${delimiter}`;
    
    header += `pcex_completed_set${delimiter}pcex_ex_dist_seen${delimiter}pcex_ch_attempts${delimiter}pcex_ch_attempts_success${delimiter}pcex_ch_dist${delimiter}pcex_ch_dist_success${delimiter}pcex_success_first_attempt${delimiter}pcex_success_second_attempt${delimiter}pcex_success_third_attempt${delimiter}pcex_success_first_attempt_first_half${delimiter}pcex_success_second_attempt_first_half${delimiter}pcex_success_third_attempt_first_half${delimiter}pcex_success_first_attempt_second_half${delimiter}pcex_success_second_attempt_second_half${delimiter}pcex_success_third_attempt_second_half${delimiter}`;
    
    header += `mg_total_loads${delimiter}mg_topic_cell_clicks${delimiter}mg_topic_cell_clicks_me${delimiter}mg_topic_cell_clicks_grp${delimiter}mg_topic_cell_clicks_mevsgrp${delimiter}mg_activity_cell_clicks${delimiter}mg_activity_cell_clicks_me${delimiter}mg_activity_cell_clicks_grp${delimiter}mg_activity_cell_clicks_mevsgrp${delimiter}`;
    header += `mg_load_rec${delimiter}mg_load_original${delimiter}mg_difficulty_feedback${delimiter}mg_change_comparison_mode${delimiter}mg_change_group${delimiter}mg_change_resource_set${delimiter}mg_load_others${delimiter}`;
    header += `mg_grid_activity_cell_mouseover${delimiter}mg_grid_topic_cell_mouseover${delimiter}mg_cm_concept_mouseover${delimiter}`;
    header += `mg_act_open_not_attempted${delimiter}mg_act_open_and_attempted${delimiter}mg_act_open_not_attempted_difficulty${delimiter}mg_act_open_and_attempted_difficulty${delimiter}mg_act_open_not_attempted_difficulty_early${EARLY_ATT_TH}${delimiter}mg_act_open_and_attempted_difficulty_early${EARLY_ATT_TH}${delimiter}`;
    
    header += `total_durationseconds${delimiter}quizjet_durationseconds${delimiter}sqlknot_durationseconds${delimiter}sqllab_durationseconds${delimiter}webex_durationseconds${delimiter}animated_example_durationseconds${delimiter}parsons_durationseconds${delimiter}parsons_durationseconds_median${delimiter}lesslet_durationseconds${delimiter}lesslet_description_durationseconds${delimiter}lesslet_example_durationseconds${delimiter}lesslet_test_durationseconds${delimiter}pcrs_durationseconds${delimiter}pcrs_durationseconds_first_attempt${delimiter}pcrs_durationseconds_second_attempt${delimiter}pcrs_durationseconds_third_attempt${delimiter}sqltutor_durationseconds${delimiter}dbqa_durationseconds${delimiter}pcex_example_durationseconds${delimiter}pcex_example_durationseconds_median${delimiter}pcex_example_lines_durationseconds${delimiter}pcex_challenge_durationseconds${delimiter}pcex_challenge_durationseconds_median${delimiter}pcex_challenge_durationseconds_first_attempt${delimiter}pcex_challenge_durationseconds_second_attempt${delimiter}pcex_challenge_durationseconds_third_attempt${delimiter}pcex_control_explanations_seen${delimiter}pcex_control_explanations_not_seen${delimiter}mastery_grid_durationseconds`;
    
    if (timebins && timebins.length > 0) {
      for (let j = 0; j <= timebins.length; j++) {
        header += `${delimiter}mg_bin${j}_act_opened_att${delimiter}mg_bin${j}_act_opened_notatt${delimiter}mg_bin${j}_act_interface${delimiter}mg_bin${j}_time${delimiter}mg_bin${j}_time_interface${delimiter}mg_bin${j}_act_opened_att_DIFF${delimiter}mg_bin${j}_act_opened_noatt_DIFF`;
      }
    }
    
    header += '\n';
    return header;
  }

  private formatUserSummary(user: User, groupId: string, timebins?: number[] | null): string {
    const delimiter = config.delimiter;
    const s = user.summary;
    
    let row = `${user.userLogin}${delimiter}${groupId}${delimiter}${s.sessions_dist || 0}${delimiter}${s.median_sessions_act ?? -1}${delimiter}${s.median_sessions_time ?? -1}${delimiter}${s.median_sessions_self_assesment ?? -1}${delimiter}${s.median_sessions_example_lines ?? -1}${delimiter}${s.topics_covered || 0}${delimiter}${s.parsons_topics_covered || 0}${delimiter}${s.pcex_topics_covered || 0}${delimiter}${s.sqlknot_topics_covered || 0}${delimiter}${s.question_attempts || 0}${delimiter}${s.question_attempts_success || 0}${delimiter}${s.questions_dist || 0}${delimiter}${s.questions_dist_success || 0}${delimiter}${s.questions_sucess_first_attempt || 0}${delimiter}${s.questions_sucess_second_attempt || 0}${delimiter}${s.questions_sucess_third_attempt || 0}${delimiter}${s.sql_knot_attempts || 0}${delimiter}${s.sql_lab_attempts || 0}${delimiter}${s.sqlknot_sucess_first_attempt || 0}${delimiter}${s.sqlknot_sucess_second_attempt || 0}${delimiter}${s.sqlknot_sucess_third_attempt || 0}${delimiter}${s.examples_dist || 0}${delimiter}${s.example_lines_actions || 0}${delimiter}${s.animated_examples_dist || 0}${delimiter}${s.animated_example_lines_actions || 0}${delimiter}`;
    
    row += `${s.parsons_attempts || 0}${delimiter}${s.parsons_attempts_success || 0}${delimiter}${s.parsons_dist || 0}${delimiter}${s.parsons_dist_success || 0}${delimiter}${s.parsons_sucess_first_attempt || 0}${delimiter}${s.parsons_sucess_second_attempt || 0}${delimiter}${s.parsons_sucess_third_attempt || 0}${delimiter}${s.parsons_sucess_first_attempt_first_half || 0}${delimiter}${s.parsons_sucess_second_attempt_first_half || 0}${delimiter}${s.parsons_sucess_third_attempt_first_half || 0}${delimiter}${s.parsons_sucess_first_attempt_second_half || 0}${delimiter}${s.parsons_sucess_second_attempt_second_half || 0}${delimiter}${s.parsons_sucess_third_attempt_second_half || 0}${delimiter}`;
    
    row += `${s.lesslet_attempts || 0}${delimiter}${s.lesslet_attempts_success || 0}${delimiter}${s.lesslet_dist || 0}${delimiter}${s.lesslet_dist_success || 0}${delimiter}${s.lesslet_description_seen || 0}${delimiter}${s.lesslet_dist_description_seen || 0}${delimiter}${s.lesslet_example_seen || 0}${delimiter}${s.lesslet_dist_example_seen || 0}${delimiter}`;
    
    row += `${s.pcrs_attempts || 0}${delimiter}${s.pcrs_attempts_success || 0}${delimiter}${s.pcrs_dist || 0}${delimiter}${s.pcrs_dist_success || 0}${delimiter}${s.pcrs_success_first_attempt || 0}${delimiter}${s.pcrs_success_second_attempt || 0}${delimiter}${s.pcrs_success_third_attempt || 0}${delimiter}`;
    
    row += `${s.sqltutor_attempts || 0}${delimiter}${s.sqltutor_attempts_success || 0}${delimiter}${s.sqltutor_dist || 0}${delimiter}${s.sqltutor_dist_success || 0}${delimiter}`;
    
    row += `${s.dbqa_steps || 0}${delimiter}${s.dbqa_final_steps || 0}${delimiter}${s.dbqa_dist || 0}${delimiter}${s.dbqa_dist_completed || 0}${delimiter}`;
    
    row += `${s.pcex_completed_set || 0}${delimiter}${s.pcex_ex_dist_seen || 0}${delimiter}${s.pcex_ch_attempts || 0}${delimiter}${s.pcex_ch_attempts_success || 0}${delimiter}${s.pcex_ch_dist || 0}${delimiter}${s.pcex_ch_dist_success || 0}${delimiter}${s.pcex_success_first_attempt || 0}${delimiter}${s.pcex_success_second_attempt || 0}${delimiter}${s.pcex_success_third_attempt || 0}${delimiter}${s.pcex_sucess_first_attempt_first_half || 0}${delimiter}${s.pcex_sucess_second_attempt_first_half || 0}${delimiter}${s.pcex_sucess_third_attempt_first_half || 0}${delimiter}${s.pcex_sucess_first_attempt_second_half || 0}${delimiter}${s.pcex_sucess_second_attempt_second_half || 0}${delimiter}${s.pcex_sucess_third_attempt_second_half || 0}${delimiter}`;
    
    row += `${s.mg_total_loads || 0}${delimiter}${s.mg_topic_cell_clicks || 0}${delimiter}${s.mg_topic_cell_clicks_me || 0}${delimiter}${s.mg_topic_cell_clicks_grp || 0}${delimiter}${s.mg_topic_cell_clicks_mevsgrp || 0}${delimiter}${s.mg_activity_cell_clicks || 0}${delimiter}${s.mg_activity_cell_clicks_me || 0}${delimiter}${s.mg_activity_cell_clicks_grp || 0}${delimiter}${s.mg_activity_cell_clicks_mevsgrp || 0}${delimiter}`;
    row += `${s.mg_load_rec || 0}${delimiter}${s.mg_load_original || 0}${delimiter}${s.mg_difficulty_feedback || 0}${delimiter}${s.mg_change_comparison_mode || 0}${delimiter}${s.mg_change_group || 0}${delimiter}${s.mg_change_resource_set || 0}${delimiter}${s.mg_load_others || 0}${delimiter}`;
    row += `${s.mg_grid_activity_cell_mouseover || 0}${delimiter}${s.mg_grid_topic_cell_mouseover || 0}${delimiter}${s.mg_cm_concept_mouseover || 0}${delimiter}`;
    row += `${s.mg_act_open_not_attempted || 0}${delimiter}${s.mg_act_open_and_attempted || 0}${delimiter}${s.mg_act_open_not_attempted_difficulty ?? -1}${delimiter}${s.mg_act_open_and_attempted_difficulty ?? -1}${delimiter}${s[`mg_act_open_not_attempted_difficulty_early${EARLY_ATT_TH}`] ?? -1}${delimiter}${s[`mg_act_open_and_attempted_difficulty_early${EARLY_ATT_TH}`] ?? -1}${delimiter}`;
    
    // Note: header says "total_durationseconds" but summary key is "durationseconds_total"
    row += `${s.durationseconds_total || 0}${delimiter}${s.durationseconds_quizjet || 0}${delimiter}${s.durationseconds_sqlknot || 0}${delimiter}${s.durationseconds_sqllab || 0}${delimiter}${s.durationseconds_webex || 0}${delimiter}${s.durationseconds_animated_example || 0}${delimiter}${s.durationseconds_parsons || 0}${delimiter}${s.durationseconds_parsons_median || 0}${delimiter}${s.durationseconds_lesslet || 0}${delimiter}${s.durationseconds_lesslet_description || 0}${delimiter}${s.durationseconds_lesslet_example || 0}${delimiter}${s.durationseconds_lesslet_test || 0}${delimiter}${s.durationseconds_pcrs || 0}${delimiter}${s.durationseconds_pcrs_first_attempt || 0}${delimiter}${s.durationseconds_pcrs_second_attempt || 0}${delimiter}${s.durationseconds_pcrs_third_attempt || 0}${delimiter}${s.durationseconds_sqltutor || 0}${delimiter}${s.durationseconds_dbqa || 0}${delimiter}${s.durationseconds_pcex_ex || 0}${delimiter}${s.durationseconds_pcex_ex_median || 0}${delimiter}${s.durationseconds_pcex_ex_lines || 0}${delimiter}${s.durationseconds_pcex_ch || 0}${delimiter}${s.durationseconds_pcex_ch_median || 0}${delimiter}${s.durationseconds_pcex_ch_first_attempt || 0}${delimiter}${s.durationseconds_pcex_ch_second_attempt || 0}${delimiter}${s.durationseconds_pcex_ch_third_attempt || 0}${delimiter}${s.durationseconds_pcex_control_explanations || 0}${delimiter}${s.durationseconds_pcex_control_no_explanations || 0}${delimiter}${s.durationseconds_mastery_grid || 0}`;
    
    if (timebins && timebins.length > 0) {
      for (let j = 0; j <= timebins.length; j++) {
        row += `${delimiter}${s[`mg_bin${j}_act_opened_att`] || 0}${delimiter}${s[`mg_bin${j}_act_opened_notatt`] || 0}${delimiter}${s[`mg_bin${j}_act_interface`] || 0}${delimiter}${s[`mg_bin${j}_time`] || 0}${delimiter}${s[`mg_bin${j}_time_interface`] || 0}${delimiter}${s[`mg_bin${j}_act_opened_att_DIFF`] ?? -1}${delimiter}${s[`mg_bin${j}_act_opened_notatt_DIFF`] ?? -1}`;
      }
    }
    
    row += '\n';
    return row;
  }

  private formatEmptyUser(userLogin: string, groupId: string, timebins?: number[] | null): string {
    // Create a minimal user object with empty summary - formatUserSummary will handle zeros
    const emptyUser = {
      userLogin,
      summary: {} as Record<string, number>,
    } as User;
    
    // Use formatUserSummary which already handles missing values as 0
    return this.formatUserSummary(emptyUser, groupId, timebins);
  }
}

