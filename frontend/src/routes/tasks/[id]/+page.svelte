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
				class="inline-flex items-center gap-1.5 font-mono text-xs uppercase tracking-wider text-[#6A84AC] hover:text-[#1446A0] transition-colors"
			>
				<span>← Back to Task Directory</span>
			</a>
		</div>

		<!-- Error Banner -->
		<ApiError message={errorMessage} onretry={loadTask} />

		{#if loading}
			<div class="space-y-6">
				<div class="h-40 border border-[#C8D4E8] bg-white animate-pulse"></div>
				<div class="h-64 border border-[#C8D4E8] bg-white animate-pulse"></div>
			</div>
		{:else if task}
			<div class="space-y-8">
				<!-- Main Card (Sharp, Cape Cobalt) -->
				<div class="border border-[#C8D4E8] bg-white p-8 shadow-xs border-t-4 border-t-[#1446A0]">
					<!-- Top Meta -->
					<div class="flex items-center justify-between gap-4 mb-4 flex-wrap">
						<div class="flex items-center gap-2">
							<span class="bg-[#EFF4FF] border border-[#C8D4E8] px-2.5 py-0.5 text-xs font-mono font-bold text-[#1446A0]">
								{task.category}
							</span>
							<StatusBadge status={task.status} size="md" />
						</div>

						<span class="font-mono text-xs text-[#6A84AC]">
							Posted {formatDate(task.createdAt, true)}
						</span>
					</div>

					<!-- Title (Serif Display) -->
					<h1 class="font-serif text-3xl sm:text-4xl text-[#0D1B3E] font-normal leading-snug">
						{task.title}
					</h1>

					<!-- SME Issuer -->
					<div class="mt-4 flex items-center gap-3 p-3 bg-[#F8F9FC] border border-[#C8D4E8] max-w-md">
						<div>
							<div class="font-mono text-[10px] uppercase tracking-wider text-[#6A84AC]">Issuer / SME Partner</div>
							<div class="font-sans text-sm font-bold text-[#0D1B3E]">{task.smeBusinessName || 'Verified SME Partner'}</div>
						</div>
					</div>

					<!-- Key Parameters Grid (Sharp Boxes) -->
					<div class="mt-8 pt-6 border-t border-[#C8D4E8] grid grid-cols-2 sm:grid-cols-4 gap-4">
						<div class="border border-[#C8D4E8] bg-[#F8F9FC] p-4">
							<div class="font-mono text-[10px] font-bold uppercase tracking-wider text-[#6A84AC]">Corporate Reserve</div>
							<div class="font-mono text-xl font-bold text-[#1446A0] mt-1">{formatCurrency(task.budget)}</div>
							<span class="font-mono text-[10px] text-[#1A8A55] uppercase">Direct Payout</span>
						</div>

						<div class="border border-[#C8D4E8] bg-[#F8F9FC] p-4">
							<div class="font-mono text-[10px] font-bold uppercase tracking-wider text-[#6A84AC]">Duration</div>
							<div class="font-mono text-xl font-semibold text-[#0D1B3E] mt-1">{formatDuration(task.durationDays)}</div>
							<span class="font-mono text-[10px] text-[#6A84AC] uppercase">Milestone Track</span>
						</div>

						<div class="border border-[#C8D4E8] bg-[#F8F9FC] p-4">
							<div class="font-mono text-[10px] font-bold uppercase tracking-wider text-[#6A84AC]">Assigned Candidate</div>
							<div class="font-sans text-sm font-bold text-[#0D1B3E] mt-1 truncate">
								{task.assignedToName || 'Unassigned'}
							</div>
							<span class="font-mono text-[10px] text-[#6A84AC] uppercase">
								{task.assignedToId ? `ID #${task.assignedToId}` : 'Open to claim'}
							</span>
						</div>

						<div class="border border-[#C8D4E8] bg-[#F8F9FC] p-4">
							<div class="font-mono text-[10px] font-bold uppercase tracking-wider text-[#6A84AC]">Compensation</div>
							<div class="font-mono text-sm font-bold text-[#1A8A55] mt-1">
								{task.isPaid ? 'PAID TASK' : 'VOLUNTEER'}
							</div>
							<span class="font-mono text-[10px] text-[#6A84AC] uppercase">Escrow Locked</span>
						</div>
					</div>

					<!-- Task Description Body -->
					<div class="mt-8">
						<h3 class="font-mono text-xs font-bold uppercase tracking-wider text-[#0D1B3E] mb-3">
							Deliverables &amp; Description
						</h3>
						<div class="font-sans text-sm text-[#465A7A] leading-relaxed whitespace-pre-line border border-[#C8D4E8] bg-[#F8F9FC] p-6">
							{task.description || 'No additional instructions specified by the SME partner.'}
						</div>
					</div>

					<!-- Role-Specific Action Bar -->
					<div class="mt-8 pt-6 border-t border-[#C8D4E8] flex flex-wrap items-center justify-between gap-4">
						<div class="font-mono text-xs text-[#6A84AC]">
							Status: <span class="font-bold text-[#0D1B3E]">{task.status}</span>
						</div>

						<div class="flex items-center gap-3">
							<!-- YOUTH ACTIONS -->
							{#if isYouth}
								{#if task.status === 'OPEN'}
									<button
										type="button"
										disabled={actionLoading}
										class="cape-btn-primary px-6 py-2.5 text-xs font-mono uppercase tracking-wider disabled:opacity-50"
										onclick={handleApplyTask}
									>
										{actionLoading ? 'Claiming...' : 'Claim / Apply for Task'}
									</button>
								{:else if task.status === 'IN_PROGRESS'}
									<button
										type="button"
										disabled={actionLoading}
										class="bg-[#1A8A55] text-white px-6 py-2.5 text-xs font-mono uppercase tracking-wider font-bold hover:bg-[#146e43] transition-colors disabled:opacity-50"
										onclick={handleCompleteTask}
									>
										{actionLoading ? 'Submitting...' : 'Mark as Completed'}
									</button>
								{:else if task.status === 'COMPLETED'}
									<span class="border border-[#C49420] bg-[#FFF8E6] px-4 py-2 text-xs font-mono font-bold text-[#C49420] uppercase">
										Awaiting SME Evaluation
									</span>
								{:else if task.status === 'VERIFIED'}
									<span class="border border-[#7C3FE4] bg-[#F5EEFF] px-4 py-2 text-xs font-mono font-bold text-[#7C3FE4] uppercase">
										✓ Verified in Experience Ledger
									</span>
								{/if}
							{/if}

							<!-- SME ACTIONS -->
							{#if isSme || user?.role === 'ADMIN'}
								{#if task.status === 'OPEN'}
									<button
										type="button"
										class="cape-btn-primary px-5 py-2.5 text-xs font-mono uppercase tracking-wider"
										onclick={() => {
											youthIdToAssign = '1';
											assignModalOpen = true;
										}}
									>
										Assign to Candidate
									</button>
								{:else if task.status === 'IN_PROGRESS' || task.status === 'COMPLETED'}
									<button
										type="button"
										class="bg-[#7C3FE4] text-white px-5 py-2.5 text-xs font-mono uppercase tracking-wider font-bold hover:bg-[#682ec7] transition-colors"
										onclick={() => {
											verifyRating = 5;
											verifyFeedback = 'Exceptional work delivered with high accuracy.';
											verifyModalOpen = true;
										}}
									>
										Verify Experience
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

<!-- Modal: Assign Task to Youth (Sharp) -->
<Modal
	bind:open={assignModalOpen}
	title={`Assign Task: ${task?.title || ''}`}
>
	<div class="space-y-4">
		<p class="text-xs text-[#465A7A] leading-relaxed font-sans">
			Specify the Youth Candidate ID to assign this task.
		</p>
		<div>
			<label for="modalYouthId" class="block text-xs font-mono font-bold uppercase tracking-wider text-[#0D1B3E]">
				Candidate ID *
			</label>
			<input
				id="modalYouthId"
				type="number"
				min="1"
				required
				bind:value={youthIdToAssign}
				placeholder="1"
				class="cape-input mt-1.5 block w-full px-4 py-2.5 text-sm font-mono"
			/>
		</div>
	</div>

	{#snippet footer()}
		<button
			type="button"
			class="cape-btn-secondary px-4 py-2 text-xs font-mono uppercase tracking-wider"
			onclick={() => (assignModalOpen = false)}
		>
			Cancel
		</button>
		<button
			type="button"
			disabled={actionLoading}
			class="cape-btn-primary px-5 py-2 text-xs font-mono uppercase tracking-widest disabled:opacity-50"
			onclick={handleAssignSubmit}
		>
			{actionLoading ? 'Assigning...' : 'Confirm Assignment'}
		</button>
	{/snippet}
</Modal>

<!-- Modal: Verify Experience (Sharp) -->
<Modal
	bind:open={verifyModalOpen}
	title={`Verify Experience: ${task?.title || ''}`}
>
	<div class="space-y-4">
		<p class="text-xs text-[#465A7A] leading-relaxed font-sans">
			Your rating and feedback will be added to the permanent Experience Ledger and release corporate wage payment.
		</p>

		<div>
			<div class="block text-xs font-mono font-bold uppercase tracking-wider text-[#0D1B3E] mb-1.5">
				Performance Rating (0 to 5 Stars) *
			</div>
			<StarRating bind:rating={verifyRating} interactive={true} size="lg" />
		</div>

		<div>
			<label for="detailFeedback" class="block text-xs font-mono font-bold uppercase tracking-wider text-[#0D1B3E]">
				Written Feedback &amp; Capability Assessment *
			</label>
			<textarea
				id="detailFeedback"
				rows="3"
				bind:value={verifyFeedback}
				placeholder="Provide feedback on communication, turnaround time, quality..."
				class="cape-input mt-1.5 block w-full px-4 py-2.5 text-sm font-sans"
			></textarea>
		</div>
	</div>

	{#snippet footer()}
		<button
			type="button"
			class="cape-btn-secondary px-4 py-2 text-xs font-mono uppercase tracking-wider"
			onclick={() => (verifyModalOpen = false)}
		>
			Cancel
		</button>
		<button
			type="button"
			disabled={actionLoading}
			class="bg-[#7C3FE4] text-white px-5 py-2 text-xs font-mono uppercase tracking-widest font-bold hover:bg-[#682ec7] disabled:opacity-50"
			onclick={handleVerifySubmit}
		>
			{actionLoading ? 'Verifying...' : 'Sign & Record to Ledger'}
		</button>
	{/snippet}
</Modal>
