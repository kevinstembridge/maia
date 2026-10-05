import {Component, inject} from '@angular/core';
import {FormArray, FormControl, FormGroup, NonNullableFormBuilder, ReactiveFormsModule, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogActions, MatDialogContent, MatDialogRef, MatDialogTitle} from '@angular/material/dialog';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatSelectModule} from '@angular/material/select';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {ActivationStrategyDefinitionDto, ActivationStrategyParameterDefinitionDto} from '../../models/ActivationStrategyDefinitionDto';
import {ActivationStrategyDescriptor} from '../../models/ActivationStrategyDescriptor';
import {parameterDefinitionsFor, StrategyRowValue, toDescriptors} from '../../state/toggles-strategy-editing';

export interface EditStrategiesDialogData {
    featureName: string;
    strategies: ActivationStrategyDescriptor[];
    definitions: ActivationStrategyDefinitionDto[];
}

export interface EditStrategiesDialogResult {
    strategies: ActivationStrategyDescriptor[];
}

// Its controls are added and removed at runtime as the selected strategy changes.
type ParametersForm = FormGroup;
type StrategyRowForm = FormGroup<{ id: FormControl<string>; parameters: ParametersForm }>;

@Component({
    selector: 'maia-edit-strategies-dialog',
    templateUrl: './edit-strategies-dialog.html',
    styleUrl: './edit-strategies-dialog.scss',
    imports: [ReactiveFormsModule, MatDialogTitle, MatDialogContent, MatDialogActions, MatFormFieldModule, MatInputModule, MatSelectModule, MatButtonModule, MatIconModule]
})
export class EditStrategiesDialog {

    readonly data = inject<EditStrategiesDialogData>(MAT_DIALOG_DATA);

    private readonly dialogRef = inject(MatDialogRef<EditStrategiesDialog>);

    private readonly formBuilder = inject(NonNullableFormBuilder);

    readonly rows: FormArray<StrategyRowForm> = this.formBuilder.array<StrategyRowForm>([]);

    readonly form = this.formBuilder.group({rows: this.rows});

    // Parameters saved for each row at open time, used to keep the parameters of strategies that
    // the server no longer knows about.
    private readonly savedParametersByRow = new Map<StrategyRowForm, ActivationStrategyDescriptor['parameters']>();

    constructor() {

        this.data.strategies.forEach(strategy => this.addRow(strategy));

    }

    addRow(strategy?: ActivationStrategyDescriptor): void {

        const row: StrategyRowForm = this.formBuilder.group({
            id: this.formBuilder.control(strategy?.id ?? '', Validators.required),
            parameters: new FormGroup({}),
        });

        this.savedParametersByRow.set(row, strategy?.parameters ?? []);
        this.rebuildParameters(row, strategy?.parameters ?? []);
        row.controls.id.valueChanges.subscribe(() => this.rebuildParameters(row, []));

        this.rows.push(row);

    }

    removeRow(index: number): void {

        this.savedParametersByRow.delete(this.rows.at(index));
        this.rows.removeAt(index);

    }

    parameterDefinitions(row: StrategyRowForm): ActivationStrategyParameterDefinitionDto[] {

        return parameterDefinitionsFor(
            row.controls.id.value,
            this.data.definitions,
            this.savedParametersByRow.get(row) ?? []
        );

    }

    /** The strategy ids to offer for a row, including its current id if the server doesn't know it. */
    optionsFor(row: StrategyRowForm): { id: string; label: string }[] {

        const options = this.data.definitions.map(d => ({id: d.id, label: d.id}));
        const currentId = row.controls.id.value;

        if (currentId !== '' && !options.some(o => o.id === currentId)) {
            options.push({id: currentId, label: `${currentId} (unknown)`});
        }

        return options;

    }

    descriptionFor(row: StrategyRowForm): string | null {

        return this.data.definitions.find(d => d.id === row.controls.id.value)?.description ?? null;

    }

    onSubmit(): void {

        if (this.form.invalid) {
            this.form.markAllAsTouched();
            return;
        }

        const rowValues: StrategyRowValue[] = this.rows.controls.map(row => ({
            id: row.controls.id.value,
            parameters: row.controls.parameters.getRawValue() as Record<string, string>,
        }));

        const result: EditStrategiesDialogResult = {strategies: toDescriptors(rowValues)};
        this.dialogRef.close(result);

    }

    onCancel(): void {
        this.dialogRef.close();
    }

    private rebuildParameters(row: StrategyRowForm, savedParameters: ActivationStrategyDescriptor['parameters']): void {

        const parameters = row.controls.parameters;

        Object.keys(parameters.controls).forEach(name => parameters.removeControl(name));

        const definitions = parameterDefinitionsFor(row.controls.id.value, this.data.definitions, this.savedParametersByRow.get(row) ?? []);

        definitions.forEach(definition => {
            const savedValue = savedParameters.find(p => p.name === definition.name)?.value ?? '';
            parameters.addControl(
                definition.name,
                this.formBuilder.control(savedValue, definition.required ? Validators.required : [])
            );
        });

    }

}
