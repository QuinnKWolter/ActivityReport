export class PCEXActivity {
  actName: string;
  isChallenge: boolean;
  contentName: string;
  topicName: string;
  providerId: string;
  orderInCourse: number;
  displayOrder: number;
  topicOrderInCourse: number;

  constructor(
    actName: string,
    isChallenge: boolean,
    contentName: string,
    topicName: string,
    providerId: string,
    orderInCourse: number,
    displayOrder: number,
    topicOrderInCourse: number
  ) {
    this.actName = actName;
    this.isChallenge = isChallenge;
    this.contentName = contentName;
    this.topicName = topicName;
    this.providerId = providerId;
    this.orderInCourse = orderInCourse;
    this.displayOrder = displayOrder;
    this.topicOrderInCourse = topicOrderInCourse;
  }

  getFirstTopic(): string {
    return this.topicName;
  }
}

