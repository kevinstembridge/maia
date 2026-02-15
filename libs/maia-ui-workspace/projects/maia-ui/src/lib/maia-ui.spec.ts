import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MaiaUi } from './maia-ui';

describe('MaiaUi', () => {
  let component: MaiaUi;
  let fixture: ComponentFixture<MaiaUi>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MaiaUi]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MaiaUi);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
