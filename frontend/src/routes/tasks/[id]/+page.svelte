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
	let applicants = $state([]);
	let loadingApplicants = $state(false);
	let verifyModalOpen = $state(false);
	let verifyRating = $state(5);
	let verifyFeedback = $state('');
	let verifyReference = $state('');
	let rejectModalOpen = $state(false);
	let rejectReason = $state('');

	// The logged-in youth's application for this task (if any)
	let myApplication = $state(null);

	const isYouth = $derived(user?.role === 'YOUTH');
	const isSme = $derived(user?.role === 'SME');
	const isAssignedToMe = $derived(
		isYouth && myApplication?.status === 'ACCEPTED'
	);

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

	async function loadMyApplication() {
		if (!task) return;
		try {
			const apps = await api.applications.mine();
			myApplication =
				(apps || []).find((a) => Number(a.taskId) === Number(task.id)) || null;
		} catch (appErr) {
			console.warn('Could not load my application:', appErr);
		}
	}

	// Wait for auth hydration so the youth-only application lookup has a role.
	$effect(() => {
		if (isYouth && task) {
			loadMyApplication();
		}
	});

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
		if (!task) return;
		actionLoading = true;
		try {
			myApplication = await api.applications.apply(task.id, '');
			toast.success(
				'Application submitted. The task is reserved for you for 48 hours while the SME reviews it.'
			);
		} catch (err) {
			toast.error(err.message || 'Failed to apply for task.');
		} finally {
			actionLoading = false;
		}
	}

	async function handleWithdrawApplication() {
		if (!myApplication) return;
		actionLoading = true;
		try {
			await api.applications.withdraw(myApplication.id);
			myApplication = { ...myApplication, status: 'WITHDRAWN' };
			toast.success('Application withdrawn. Your reservation has been released.');
		} catch (err) {
			toast.error(err.message || 'Failed to withdraw application.');
		} finally {
			actionLoading = false;
		}
	}

	async function openAssignModal() {
		if (!task) return;
		assignModalOpen = true;
		loadingApplicants = true;
		applicants = [];
		youthIdToAssign = '';
		try {
			const res = await api.applications.forTask(task.id);
			applicants = (res || []).filter((a) => a.status === 'PENDING');
			if (applicants.length > 0) {
				youthIdToAssign = String(applicants[0].youthId);
			}
		} catch (err) {
			toast.error(err.message || 'Failed to load applicants.');
		} finally {
			loadingApplicants = false;
		}
	}

	async function handleAssignSubmit() {
		if (!task || !youthIdToAssign) return;
		actionLoading = true;
		try {
			const res = await api.tasks.assign(task.id, youthIdToAssign);
			task = res;
			toast.success('Task assigned to the selected applicant!');
			assignModalOpen = false;
		} catch (err) {
			toast.error(err.message || 'Failed to assign task.');
		} finally {
			actionLoading = false;
		}
	}

	function openVerifyModal() {
		verifyRating = 5;
		verifyFeedback = '';
		verifyReference = '';
		verifyModalOpen = true;
	}

	async function handleVerifySubmit() {
		if (!task) return;
		if (!verifyFeedback.trim()) {
			toast.error('Written feedback is required.');
			return;
		}
		if (!verifyReference.trim()) {
			toast.error('A reference to the work you checked is required.');
			return;
		}
		actionLoading = true;
		try {
			await api.tasks.verify(
				task.id,
				verifyFeedback.trim(),
				verifyRating,
				verifyReference.trim()
			);
			toast.success('Experience verified and recorded to Experience Ledger!');
			verifyModalOpen = false;
			await loadTask();
		} catch (err) {
			toast.error(err.message || 'Failed to verify experience.');
		} finally {
			actionLoading = false;
		}
	}

	function openRejectModal() {
		rejectReason = '';
		rejectModalOpen = true;
	}

	async function handleRejectSubmit() {
		if (!task) return;
		if (!rejectReason.trim()) {
			toast.error('A revision reason is required.');
			return;
		}
		actionLoading = true;
		try {
			const res = await api.tasks.reject(task.id, rejectReason.trim());
			task = res;
			toast.success('Task sent back to the youth for revision.');
			rejectModalOpen = false;
		} catch (err) {
			toast.error(err.message || 'Failed to send task back for revision.');
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

					<!-- Revision Request Banner -->
					{#if task.revisionReason}
						<div class="mt-6 border-l-4 border-l-[#C49420] border border-[#C8D4E8] bg-[#FFF8E6] p-4">
							<div class="font-mono text-[10px] font-bold uppercase tracking-wider text-[#8A6A12]">
								SME Requested a Revision
								{#if task.revisionCount}
									· Round {task.revisionCount}
								{/if}
							</div>
							<p class="mt-1 font-sans text-sm text-[#465A7A]">{task.revisionReason}</p>
						</div>
					{/if}

					<!-- Role-Specific Action Bar -->
					<div class="mt-8 pt-6 border-t border-[#C8D4E8] flex flex-wrap items-center justify-between gap-4">
						<div class="font-mono text-xs text-[#6A84AC]">
							Status: <span class="font-bold text-[#0D1B3E]">{task.status}</span>
						</div>

						<div class="flex items-center gap-3 flex-wrap">
							<!-- YOUTH ACTIONS -->
							{#if isYouth}
								{#if task.status === 'OPEN'}
									{#if myApplication?.status === 'PENDING'}
										<span class="border border-[#C49420] bg-[#FFF8E6] px-4 py-2 text-xs font-mono font-bold text-[#C49420] uppercase">
											Application Pending · Reserved 48h
										</span>
										<button
											type="button"
											disabled={actionLoading}
											class="cape-btn-secondary px-4 py-2 text-xs font-mono uppercase tracking-wider disabled:opacity-50"
											onclick={handleWithdrawApplication}
										>
											Withdraw
										</button>
									{:else if myApplication?.status === 'ACCEPTED'}
										<span class="border border-[#1A8A55] bg-[#EDFBF4] px-4 py-2 text-xs font-mono font-bold text-[#1A8A55] uppercase">
											Accepted · Awaiting Task Start
										</span>
									{:else}
										<button
											type="button"
											disabled={actionLoading}
											class="cape-btn-primary px-6 py-2.5 text-xs font-mono uppercase tracking-wider disabled:opacity-50"
											onclick={handleApplyTask}
										>
											{actionLoading ? 'Submitting...' : 'Apply for Task'}
										</button>
									{/if}
								{:else if task.status === 'IN_PROGRESS'}
									{#if isAssignedToMe}
										<button
											type="button"
											disabled={actionLoading}
											class="bg-[#1A8A55] text-white px-6 py-2.5 text-xs font-mono uppercase tracking-wider font-bold hover:bg-[#146e43] transition-colors disabled:opacity-50"
											onclick={handleCompleteTask}
										>
											{actionLoading ? 'Submitting...' : 'Mark as Completed'}
										</button>
									{:else}
										<span class="border border-[#C8D4E8] bg-[#F8F9FC] px-4 py-2 text-xs font-mono font-bold text-[#6A84AC] uppercase">
											Already Assigned to Another Candidate
										</span>
									{/if}
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
										onclick={openAssignModal}
									>
										Review Applicants &amp; Assign
									</button>
								{:else if task.status === 'COMPLETED'}
									<button
										type="button"
										class="bg-[#7C3FE4] text-white px-5 py-2.5 text-xs font-mono uppercase tracking-wider font-bold hover:bg-[#682ec7] transition-colors"
										onclick={openVerifyModal}
									>
										Verify Experience
									</button>
									<button
										type="button"
										class="cape-btn-secondary px-4 py-2.5 text-xs font-mono uppercase tracking-wider"
										onclick={openRejectModal}
									>
										Send Back for Revision
									</button>
								{:else if task.status === 'IN_PROGRESS'}
									<span class="border border-[#C8D4E8] bg-[#F8F9FC] px-4 py-2 text-xs font-mono font-bold text-[#6A84AC] uppercase">
										Awaiting Youth Completion
									</span>
								{:else if task.status === 'VERIFIED'}
									<span class="border border-[#7C3FE4] bg-[#F5EEFF] px-4 py-2 text-xs font-mono font-bold text-[#7C3FE4] uppercase">
										✓ Verified &amp; Payment Created
									</span>
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
			Select one of the youth who applied. Assigning accepts that application and rejects the other pending applicants.
		</p>

		{#if loadingApplicants}
			<div class="border border-[#C8D4E8] bg-[#F8F9FC] p-4 font-mono text-xs text-[#6A84AC]">
				Loading applicants...
			</div>
		{:else if applicants.length === 0}
			<div class="border border-[#C49420] bg-[#FFF8E6] p-4 font-sans text-xs text-[#8A6A12]">
				No pending applications yet. A youth must apply for this task before you can assign it.
			</div>
		{:else}
			<div>
				<label for="modalYouthId" class="block text-xs font-mono font-bold uppercase tracking-wider text-[#0D1B3E]">
					Pending Applicant *
				</label>
				<select
					id="modalYouthId"
					required
					bind:value={youthIdToAssign}
					class="cape-input mt-1.5 block w-full px-4 py-2.5 text-sm font-sans"
				>
					{#each applicants as applicant}
						<option value={String(applicant.youthId)}>
							{applicant.youthName || `Youth #${applicant.youthId}`} — applied {formatDate(applicant.appliedAt, true)}
						</option>
					{/each}
				</select>
			</div>
		{/if}
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
			disabled={actionLoading || loadingApplicants || !youthIdToAssign}
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
				Performance Rating (1 to 5 Stars) *
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

		<div>
			<label for="detailReference" class="block text-xs font-mono font-bold uppercase tracking-wider text-[#0D1B3E]">
				Evidence Reference *
			</label>
			<input
				id="detailReference"
				type="text"
				bind:value={verifyReference}
				placeholder="e.g. Invoice batch #INV-2026-04 or shared drive link"
				class="cape-input mt-1.5 block w-full px-4 py-2.5 text-sm font-sans"
			/>
			<p class="mt-1 font-sans text-[11px] text-[#6A84AC]">
				Stored on the public verification record so anyone can confirm what was checked.
			</p>
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

<!-- Modal: Send Back for Revision -->
<Modal
	bind:open={rejectModalOpen}
	title={`Request Revision: ${task?.title || ''}`}
>
	<div class="space-y-4">
		<p class="text-xs text-[#465A7A] leading-relaxed font-sans">
			Explain what must be fixed. The task returns to IN_PROGRESS for the same youth, and no payment is created until the work is verified.
		</p>

		<div>
			<label for="rejectReason" class="block text-xs font-mono font-bold uppercase tracking-wider text-[#0D1B3E]">
				Revision Reason *
			</label>
			<textarea
				id="rejectReason"
				rows="3"
				bind:value={rejectReason}
				placeholder="e.g. Please reconcile the March invoices and attach the updated spreadsheet."
				class="cape-input mt-1.5 block w-full px-4 py-2.5 text-sm font-sans"
			></textarea>
		</div>
	</div>

	{#snippet footer()}
		<button
			type="button"
			class="cape-btn-secondary px-4 py-2 text-xs font-mono uppercase tracking-wider"
			onclick={() => (rejectModalOpen = false)}
		>
			Cancel
		</button>
		<button
			type="button"
			disabled={actionLoading}
			class="bg-[#C49420] text-white px-5 py-2 text-xs font-mono uppercase tracking-widest font-bold hover:bg-[#a87c14] disabled:opacity-50"
			onclick={handleRejectSubmit}
		>
			{actionLoading ? 'Sending...' : 'Send Back for Revision'}
		</button>
	{/snippet}
</Modal>
