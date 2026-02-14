import { NotificationService } from './notification.service';

describe('NotificationService', () => {
  let service: NotificationService;

  beforeEach(() => {
    service = new NotificationService();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should emit success notification', (done) => {
    service.notification$.subscribe(n => {
      expect(n.type).toBe('success');
      expect(n.message).toBe('Test success');
      expect(n.duration).toBe(3000);
      done();
    });
    service.success('Test success');
  });

  it('should emit error notification', (done) => {
    service.notification$.subscribe(n => {
      expect(n.type).toBe('error');
      expect(n.message).toBe('Test error');
      expect(n.duration).toBe(5000);
      done();
    });
    service.error('Test error');
  });

  it('should emit info notification', (done) => {
    service.notification$.subscribe(n => {
      expect(n.type).toBe('info');
      expect(n.message).toBe('Test info');
      done();
    });
    service.info('Test info');
  });

  it('should emit warning notification', (done) => {
    service.notification$.subscribe(n => {
      expect(n.type).toBe('warning');
      expect(n.message).toBe('Test warning');
      done();
    });
    service.warning('Test warning');
  });
});
