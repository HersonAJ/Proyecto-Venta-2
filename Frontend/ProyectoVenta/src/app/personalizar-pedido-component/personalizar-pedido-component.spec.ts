import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PersonalizarPedidoComponent } from './personalizar-pedido-component';

describe('PersonalizarPedidoComponent', () => {
  let component: PersonalizarPedidoComponent;
  let fixture: ComponentFixture<PersonalizarPedidoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PersonalizarPedidoComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PersonalizarPedidoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
