import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CanjearPromoComponent } from './canjear-promo-component';

describe('CanjearPromoComponent', () => {
  let component: CanjearPromoComponent;
  let fixture: ComponentFixture<CanjearPromoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CanjearPromoComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CanjearPromoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
