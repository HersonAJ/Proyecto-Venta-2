import { ComponentFixture, TestBed } from '@angular/core/testing';

import { VentasManualesComponent } from './ventas-manuales-component';

describe('VentasManualesComponent', () => {
  let component: VentasManualesComponent;
  let fixture: ComponentFixture<VentasManualesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [VentasManualesComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(VentasManualesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
