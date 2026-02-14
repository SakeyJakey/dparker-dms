import { Injectable } from '@angular/core';
import { Subject, Observable } from 'rxjs';

export interface Notification {
  message: string;
  type: 'success' | 'error' | 'info' | 'warning';
  duration?: number;
}

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private notificationSubject = new Subject<Notification>();
  notification$: Observable<Notification> = this.notificationSubject.asObservable();

  success(message: string): void {
    this.notificationSubject.next({ message, type: 'success', duration: 3000 });
  }

  error(message: string): void {
    this.notificationSubject.next({ message, type: 'error', duration: 5000 });
  }

  info(message: string): void {
    this.notificationSubject.next({ message, type: 'info', duration: 3000 });
  }

  warning(message: string): void {
    this.notificationSubject.next({ message, type: 'warning', duration: 4000 });
  }
}
