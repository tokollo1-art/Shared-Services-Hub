<script>
	import RoleGuard from '$lib/components/RoleGuard.svelte';
	import StatusBadge from '$lib/components/StatusBadge.svelte';
	import StarRating from '$lib/components/StarRating.svelte';
	import Modal from '$lib/components/Modal.svelte';
	import ApiError from '$lib/components/ApiError.svelte';
	import { api } from '$lib/api.js';
	import { currentUser } from '$lib/stores/auth.js';
	import { toast } from '$lib/stores/toast.js';
	import { formatCurrency, formatDuration, formatDate } from '$lib/utils/formatters.js';
	import { page } from '$app/state';
	import { onMount } from 'svelte';

	const user = $derived($currentUser);
	const taskId = $derived(page.params.id);

	let task = $state(null);
	let loading = $state(true);
	let errorMessage = $state(null);
	let actionLoading = $state(false);

	// Modals
	let assignModalOpen = $state(false);
	let youthIdToAssign = $state('');
	let verifyModalOpen = $state(false);
	let verifyRating = $state(5);
	let verifyFeedback = $state('');

	const isYouth = $derived(user?.role === 'YOUTH');
	const isSme = $derived(user?.role === 'SME');
	const isAssignedToMe = $derived(isYouth && task?.assignedToId && Number(task.assignedToId) === Number(user?.id));

	async function loadTask() {
		loading = true;
		errorMessage = null;
		try {
			const res = await api.tasks.get(taskId);
			task = res;
		} catch (err) {
			console.error('Failed to load task detail:', err);
			errorMessage = err.message || 'Task not found or could not be loaded.';
		} finally {
			loading = false;
		}
	}

	// Youth Action: Mark Complete
	async function handleCompleteTask() {
		if (!task) return;
		actionLoading = true;
		try {
			const res = await api.tasks.complete(task.id);
			task = res;
			toast.success('Task marked as completed! SME has been notified to verify your work.');
		} catch (err) {
			toast.error(err.message || 'Failed to complete task.');
		} finally {
			actionLoading = false;
		}
	}

	// Youth Action: Apply / Self-Assign (when OPEN)
	async function handleApplyTask() {
		if (!task || !user) return;
		actionLoading = true;
		try {
			const res = await api.tasks.assign(task.id, user.id);
			task = res;
			toast.success('You have claimed this task! It is now IN_PROGRESS.');
		} catch (err) {
			toast.error(err.message || 'Failed to claim task.');
		} finally {
			actionLoading = false;
		}
	}

	// SME Action: Assign
	async function handleAssignSubmit() {
		if (!task || !youthIdToAssign) return;
		actionLoading = true;
		try {
			const res = await api.tasks.assign(task.id, youthIdToAssign);
			task = res;
			toast.success(`Task assigned to Candidate #${youthIdToAssign}!`);
			assignModalOpen = false;
		} catch (err) {
			toast.error(err.message || 'Failed to assign task.');
		} finally {
			actionLoading = false;
		}
	}

	// SME Action: Verify
	async function handleVerifySubmit() {
		if (!task) return;
		actionLoading = true;
		try {
			await api.experiences.verify(task.id, verifyFeedback, verifyRating);
			toast.success('Experience verified and recorded to Experience Ledger!');
			verifyModalOpen = false;
			await loadTask();
		} catch (err) {
			toast.error(err.message || 'Failed to verify experience.');
		} finally {
			actionLoading = false;
		}
	}

	onMount(() => {
		loadTask();
	});
</script>

<svelte:head>
	<title>{task ? task.title : 'Task Details'} | Shared Services Hub</title>
</svelte:head>

<RoleGuard requireAuth={true}>
	<div class="mx-auto max-w-5xl px-4 py-8 sm:px-6 lg:px-8">
		<!-- Back Button -->
		<div class="mb-6">
			<a
				href="/tasks"
				class="inline-flex items-center gap-1.5 text-xs font-semibold text-slate-500 hover:text-indigo-600 transition-colors"
			>
				<svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
					<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
				</svg>
				<span>Back to Tasks</span>
			</a>
		</div>

		<!-- Error Banner -->
		<ApiError message={errorMessage} onretry={loadTask} />

		{#if loading}
			<div class="space-y-6">
				<div class="h-40 rounded-3xl bg-slate-200/60 animate-pulse"></div>
				<div class="h-64 rounded-3xl bg-slate-200/60 animate-pulse"></div>
			</div>
		{:else if task}
			<div class="space-y-8">
				<!-- Main Card -->
				<div class="rounded-3xl border border-slate-200 bg-white p-8 shadow-sm">
					<!-- Top Meta -->
					<div class="flex items-center justify-between gap-4 mb-4 flex-wrap">
						<div class="flex items-center gap-2">
							<span class="rounded-lg bg-indigo-50 px-3 py-1 text-xs font-bold text-indigo-700 ring-1 ring-inset ring-indigo-700/10">
								{task.category}
							</span>
							<StatusBadge status={task.status} size="md" />
						</div>

						<span class="text-xs text-slate-400 font-medium">
							Posted {formatDate(task.createdAt, true)}
						</span>
					</div>

					<!-- Title -->
					<h1 class="text-3xl font-extrabold text-slate-900 tracking-tight leading-snug">
						{task.title}
					</h1>

					<!-- SME Issuer -->
					<div class="mt-4 flex items-center gap-3 p-3 rounded-2xl bg-slate-50 border border-slate-100 max-w-md">
						<div class="flex h-10 w-10 items-center justify-center rounded-xl bg-amber-100 text-amber-700 font-bold">
							<svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
								<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
							</svg>
						</div>
						<div>
							<div class="text-xs text-slate-400 font-semibold uppercase">Posting SME Partner</div>
							<div class="text-sm font-bold text-slate-900">{task.smeBusinessName || 'Verified SME Partner'}</div>
						</div>
					</div>

					<!-- Key Parameters Grid -->
					<div class="mt-8 pt-6 border-t border-slate-100 grid grid-cols-2 sm:grid-cols-4 gap-4">
						<div class="rounded-xl bg-slate-50 p-4 border border-slate-100">
							<div class="text-xs text-slate-400 font-semibold uppercase">Budget (Corporate Funded)</div>
							<div class="text-xl font-black text-indigo-600 mt-1">{formatCurrency(task.budget)}</div>
							<span class="text-[11px] text-emerald-600 font-medium">Guaranteed direct pay</span>
						</div>

						<div class="rounded-xl bg-slate-50 p-4 border border-slate-100">
							<div class="text-xs text-slate-400 font-semibold uppercase">Expected Duration</div>
							<div class="text-xl font-bold text-slate-800 mt-1">{formatDuration(task.durationDays)}</div>
							<span class="text-[11px] text-slate-500">Structured delivery</span>
						</div>

						<div class="rounded-xl bg-slate-50 p-4 border border-slate-100">
							<div class="text-xs text-slate-400 font-semibold uppercase">Assigned Talent</div>
							<div class="text-sm font-bold text-slate-800 mt-1 truncate">
								{task.assignedToName || 'Unassigned'}
							</div>
							<span class="text-[11px] text-slate-500">
								{task.assignedToId ? `ID #${task.assignedToId}` : 'Available to claim'}
							</span>
						</div>

						<div class="rounded-xl bg-slate-50 p-4 border border-slate-100">
							<div class="text-xs text-slate-400 font-semibold uppercase">Stipend Status</div>
							<div class="text-sm font-bold text-emerald-700 mt-1">
								{task.isPaid ? '✓ Paid Task' : 'Volunteer'}
							</div>
							<span class="text-[11px] text-slate-500">Sponsor backed</span>
						</div>
					</div>

					<!-- Task Description Body -->
					<div class="mt-8">
						<h3 class="text-sm font-bold uppercase tracking-wider text-slate-700 mb-3">Deliverables & Requirements</h3>
						<div class="prose prose-slate max-w-none text-slate-700 text-sm leading-relaxed whitespace-pre-line rounded-2xl bg-slate-50/50 p-6 border border-slate-200/60">
							{task.description || 'No additional instructions specified by the SME partner.'}
						</div>
					</div>

					<!-- Role-Specific Action Bar -->
					<div class="mt-8 pt-6 border-t border-slate-100 flex flex-wrap items-center justify-between gap-4">
						<div class="text-xs text-slate-500">
							Status: <span class="font-semibold text-slate-900">{task.status}</span>
						</div>

						<div class="flex items-center gap-3">
							<!-- YOUTH ACTIONS -->
							{#if isYouth}
								{#if task.status === 'OPEN'}
									<button
										type="button"
										disabled={actionLoading}
										class="rounded-xl bg-indigo-600 px-6 py-2.5 text-xs font-bold text-white shadow-md shadow-indigo-500/20 hover:bg-indigo-700 transition-all active:scale-95 disabled:opacity-50"
										onclick={handleApplyTask}
									>
										{actionLoading ? 'Claiming...' : 'Claim / Apply for Task'}
									</button>
								{:else if task.status === 'IN_PROGRESS'}
									<button
										type="button"
										disabled={actionLoading}
										class="rounded-xl bg-emerald-600 px-6 py-2.5 text-xs font-bold text-white shadow-md shadow-emerald-500/20 hover:bg-emerald-700 transition-all active:scale-95 disabled:opacity-50 flex items-center gap-1.5"
										onclick={handleCompleteTask}
									>
										<svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
											<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M5 13l4 4L19 7" />
										</svg>
										<span>{actionLoading ? 'Marking Complete...' : 'Mark as Completed'}</span>
									</button>
								{:else if task.status === 'COMPLETED'}
									<span class="rounded-xl bg-amber-50 px-4 py-2 text-xs font-bold text-amber-800 border border-amber-200">
										Completed! Awaiting SME Verification
									</span>
								{:else if task.status === 'VERIFIED'}
									<span class="rounded-xl bg-purple-50 px-4 py-2 text-xs font-bold text-purple-700 border border-purple-200">
										✓ Verified & Logged in Experience Ledger
									</span>
								{/if}
							{/if}

							<!-- SME ACTIONS -->
							{#if isSme || user?.role === 'ADMIN'}
								{#if task.status === 'OPEN'}
									<button
										type="button"
										class="rounded-xl bg-indigo-600 px-5 py-2.5 text-xs font-bold text-white shadow-xs hover:bg-indigo-700 transition-colors"
										onclick={() => {
											youthIdToAssign = '1';
											assignModalOpen = true;
										}}
									>
										Assign to Youth Candidate
									</button>
								{:else if task.status === 'IN_PROGRESS' || task.status === 'COMPLETED'}
									<button
										type="button"
										class="rounded-xl bg-purple-600 px-5 py-2.5 text-xs font-bold text-white shadow-xs hover:bg-purple-700 transition-colors flex items-center gap-1.5"
										onclick={() => {
											verifyRating = 5;
											verifyFeedback = 'Exceptional work delivered with high accuracy.';
											verifyModalOpen = true;
										}}
									>
										<span>Verify Experience</span>
										<svg class="h-3.5 w-3.5" viewBox="0 0 20 20" fill="currentColor">
											<path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd" />
										</svg>
									</button>
								{/if}
							{/if}
						</div>
					</div>
				</div>
			</div>
		{/if}
	</div>
</RoleGuard>

<!-- Modal: Assign Task to Youth -->
<Modal
	bind:open={assignModalOpen}
	title={`Assign Task: ${task?.title || ''}`}
>
	<div class="space-y-4">
		<p class="text-xs text-slate-600 leading-relaxed">
			Specify the Youth Candidate ID to assign this task.
		</p>
		<div>
			<label for="modalYouthId" class="block text-xs font-bold uppercase tracking-wider text-slate-700">
				Candidate ID *
			</label>
			<input
				id="modalYouthId"
				type="number"
				min="1"
				required
				bind:value={youthIdToAssign}
				placeholder="e.g. 1"
				class="mt-1 block w-full rounded-xl border border-slate-300 px-4 py-2.5 text-sm text-slate-900 focus:border-indigo-600 focus:outline-none focus:ring-2 focus:ring-indigo-600/20"
			/>
		</div>
	</div>

	{#snippet footer()}
		<button
			type="button"
			class="rounded-xl border border-slate-300 bg-white px-4 py-2 text-xs font-bold text-slate-700 hover:bg-slate-50"
			onclick={() => (assignModalOpen = false)}
		>
			Cancel
		</button>
		<button
			type="button"
			disabled={actionLoading}
			class="rounded-xl bg-indigo-600 px-5 py-2 text-xs font-bold text-white shadow-xs hover:bg-indigo-700 disabled:opacity-50"
			onclick={handleAssignSubmit}
		>
			{actionLoading ? 'Assigning...' : 'Confirm Assignment'}
		</button>
	{/snippet}
</Modal>

<!-- Modal: Verify Experience -->
<Modal
	bind:open={verifyModalOpen}
	title={`Verify Experience: ${task?.title || ''}`}
>
	<div class="space-y-4">
		<p class="text-xs text-slate-600 leading-relaxed">
			Your rating and feedback will be added to the permanent Experience Ledger and release corporate wage payment.
		</p>

		<div>
			<div class="block text-xs font-bold uppercase tracking-wider text-slate-700 mb-1.5">
				Performance Rating (0 to 5 Stars) *
			</div>
			<StarRating bind:rating={verifyRating} interactive={true} size="lg" />
		</div>

		<div>
			<label for="detailFeedback" class="block text-xs font-bold uppercase tracking-wider text-slate-700">
				Written Feedback & Capability Assessment *
			</label>
			<textarea
				id="detailFeedback"
				rows="3"
				bind:value={verifyFeedback}
				placeholder="Provide feedback on communication, turnaround time, quality..."
				class="mt-1 block w-full rounded-xl border border-slate-300 px-4 py-2.5 text-sm text-slate-900 focus:border-indigo-600 focus:outline-none focus:ring-2 focus:ring-indigo-600/20"
			></textarea>
		</div>
	</div>

	{#snippet footer()}
		<button
			type="button"
			class="rounded-xl border border-slate-300 bg-white px-4 py-2 text-xs font-bold text-slate-700 hover:bg-slate-50"
			onclick={() => (verifyModalOpen = false)}
		>
			Cancel
		</button>
		<button
			type="button"
			disabled={actionLoading}
			class="rounded-xl bg-purple-600 px-5 py-2 text-xs font-bold text-white shadow-xs hover:bg-purple-700 disabled:opacity-50"
			onclick={handleVerifySubmit}
		>
			{actionLoading ? 'Verifying...' : 'Sign & Record to Ledger'}
		</button>
	{/snippet}
</Modal>
