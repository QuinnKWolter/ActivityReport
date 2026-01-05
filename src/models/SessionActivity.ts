export class SessionActivity {
  private time: number = 0.0;
  public questions: Map<string, number> = new Map();
  public parsons: Map<string, number> = new Map();
  public examples: Map<string, number> = new Map();
  public animations: Map<string, number> = new Map();
  public lesslets: Map<string, number> = new Map();
  public challenges: Map<string, number> = new Map();

  addQuestion(act: string): void {
    this.questions.set(act, (this.questions.get(act) || 0) + 1);
  }

  addParson(act: string): void {
    this.parsons.set(act, (this.parsons.get(act) || 0) + 1);
  }

  addExample(act: string): void {
    this.examples.set(act, (this.examples.get(act) || 0) + 1);
  }

  addAnimation(act: string): void {
    this.animations.set(act, (this.animations.get(act) || 0) + 1);
  }

  addLesslet(act: string): void {
    this.lesslets.set(act, (this.lesslets.get(act) || 0) + 1);
  }

  addChallenge(act: string): void {
    this.challenges.set(act, (this.challenges.get(act) || 0) + 1);
  }

  addTime(time: number): void {
    this.time += time;
  }

  getTime(): number {
    return this.time;
  }

  countActivity(): number {
    let question = 0;
    let parson = 0;
    let lesslet = 0;
    let challenge = 0;
    
    for (const count of this.questions.values()) question += count;
    for (const count of this.parsons.values()) parson += count;
    for (const count of this.lesslets.values()) lesslet += count;
    for (const count of this.challenges.values()) challenge += count;
    
    // Examples and Anim Examples are not counted by attempt (each attempt is a line)
    const example = this.examples.size;
    const animation = this.animations.size;
    
    return question + parson + example + animation + lesslet + challenge;
  }

  countSelfAssesment(): number {
    let question = 0;
    let parson = 0;
    let lesslet = 0;
    let challenge = 0;
    
    for (const count of this.questions.values()) question += count;
    for (const count of this.parsons.values()) parson += count;
    for (const count of this.lesslets.values()) lesslet += count;
    for (const count of this.challenges.values()) challenge += count;
    
    return question + parson + lesslet + challenge;
  }

  countExampleLines(): number {
    let example = 0;
    for (const count of this.examples.values()) example += count;
    return example;
  }
}

