export class Activity {
  contentName: string;
  topicName: string;
  providerId: string;
  orderInCourse: number;
  displayOrder: number;
  topicOrderInCourse: number;

  constructor(
    contentName: string,
    topicName: string,
    providerId: string,
    orderInCourse: number,
    displayOrder: number,
    topicOrderInCourse: number
  ) {
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

