import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ConfirmDialogComponent } from './confirm-dialog.component';

describe('ConfirmDialogComponent', () => {
  let component: ConfirmDialogComponent;
  let fixture: ComponentFixture<ConfirmDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ConfirmDialogComponent]
    }).compileComponents();
    fixture = TestBed.createComponent(ConfirmDialogComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should not render when not visible', () => {
    component.visible = false;
    fixture.detectChanges();
    const el = fixture.nativeElement as HTMLElement;
    expect(el.querySelector('.dialog-overlay')).toBeNull();
  });

  it('should render when visible', () => {
    component.visible = true;
    fixture.detectChanges();
    const el = fixture.nativeElement as HTMLElement;
    expect(el.querySelector('.dialog-overlay')).toBeTruthy();
  });

  it('should display title and message', () => {
    component.visible = true;
    component.title = 'Delete Document';
    component.message = 'This cannot be undone.';
    fixture.detectChanges();
    const el = fixture.nativeElement as HTMLElement;
    expect(el.querySelector('h2')?.textContent).toContain('Delete Document');
    expect(el.querySelector('p')?.textContent).toContain('This cannot be undone.');
  });

  it('should emit confirmed on confirm', () => {
    component.visible = true;
    fixture.detectChanges();
    spyOn(component.confirmed, 'emit');
    component.onConfirm();
    expect(component.confirmed.emit).toHaveBeenCalled();
    expect(component.visible).toBeFalse();
  });

  it('should emit cancelled on cancel', () => {
    component.visible = true;
    fixture.detectChanges();
    spyOn(component.cancelled, 'emit');
    component.onCancel();
    expect(component.cancelled.emit).toHaveBeenCalled();
    expect(component.visible).toBeFalse();
  });

  it('should have aria-modal attribute', () => {
    component.visible = true;
    fixture.detectChanges();
    const el = fixture.nativeElement as HTMLElement;
    expect(el.querySelector('[aria-modal="true"]')).toBeTruthy();
  });
});
