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
	import { onMount } from 'svelte';

	const user = $derived($currentUser);

	let tasks = $state([]);
	let loading = $state(true);
	let errorMessage = $state(null);

	// Post a Task Form State
	let showPostForm = $state(false);
	let posting = $state(false);
	let postError = $state(null);
	let taskTitle = $state('');
	let taskCategory = $state('Digital Operations');
	let taskBudget = $state(1000);
	let taskDuration = $state(7);
	let taskPaid = $state(true);
	let taskDescription = $state('');

	// Assign Task Modal State
	let assignModalOpen = $state(false);
	let selectedTaskToAssign = $state(null);
	let youthIdToAssign = $state('');
	let assigning = $state(false);

	// Verify Experience Modal State
	let verifyModalOpen = $state(false);
	let selectedTaskToVerify = $state(null);
	let verifyRating = $state(5);
	let verifyFeedback = $state('');
	let verifying = $state(false);

	const categories = [
		'Digital Operations',
		'Marketing & Creative',
		'Accounting & Finance',
		'IT & Technical',
		'Customer Support',
		'General Administration'
	];

	async function loadTasks() {
		loading = true;
		errorMessage = null;
		try {
			const res = await api.tasks.list();
			tasks = Array.isArray(res) ? res : [];
		} catch (err) {
			console.error('Failed to load SME tasks:', err);
			errorMessage = err.message || 'Could not load your tasks.';
		} finally {
			loading = false;
		}
	}

	async function handlePostTask(event) {
		event?.preventDefault();
		if (!taskTitle.trim()) {
			postError = 'Please provide a task title.';
			return;
		}

		posting = true;
		postError = null;

		const payload = {
			title: taskTitle.trim(),
			description: taskDescription.trim(),
			category: taskCategory,
			budget: Number(taskBudget) || 0,
			durationDays: Number(taskDuration) || 7,
			isPaid: Boolean(taskPaid)
		};

		try {
			const created = await api.tasks.create(payload);
			toast.success(`Task "${created.title}" published!`);
			showPostForm = false;
			taskTitle = '';
			taskDescription = '';
			taskBudget = 1000;
			taskDuration = 7;
			await loadTasks();
		} catch (err) {
			postError = err.message || 'Failed to post task.';
		} finally {
			posting = false;
		}
	}

	function openAssignModal(task) {
		selectedTaskToAssign = task;
		youthIdToAssign = '1';
		assignModalOpen = true;
	}

	async function handleAssignSubmit() {
		if (!selectedTaskToAssign || !youthIdToAssign) return;

		assigning = true;
		try {
			await api.tasks.assign(selectedTaskToAssign.id, youthIdToAssign);
			toast.success(`Task assigned to Candidate #${youthIdToAssign}!`);
			assignModalOpen = false;
			await loadTasks();
		} catch (err) {
			toast.error(err.message || 'Failed to assign task.');
		} finally {
			assigning = false;
		}
	}

	function openVerifyModal(task) {
		selectedTaskToVerify = task;
		verifyRating = 5;
		verifyFeedback = 'Work was delivered accurately and ahead of deadline. Very dependable.';
		verifyModalOpen = true;
	}

	async function handleVerifySubmit() {
		if (!selectedTaskToVerify) return;

		verifying = true;
		try {
			const experienceId = selectedTaskToVerify.id;
			await api.experiences.verify(experienceId, verifyFeedback, verifyRating);
			toast.success('Experience verified and recorded into the Experience Ledger!');
			verifyModalOpen = false;
			await loadTasks();
		} catch (err) {
			toast.error(err.message || 'Failed to verify experience.');
		} finally {
			verifying = false;
		}
	}

	onMount(() => {
		loadTasks();
	});
</script>

<svelte:head>
	<title>SME Partner Dashboard | Shared Services Hub</title>
</svelte:head>

<RoleGuard allowedRoles={['SME', 'ADMIN']}>
	<div class="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
		<!-- Header -->
		<div class="border border-[#C8D4E8] bg-white p-8 shadow-xs mb-8 flex flex-col md:flex-row md:items-center md:justify-between gap-6 border-l-4 border-l-[#1446A0]">
			<div>
				<div class="inline-flex items-center gap-1.5 bg-[#EFF4FF] border border-[#C8D4E8] px-2.5 py-0.5 text-[10px] font-mono font-bold text-[#1446A0] uppercase mb-2">
					<span>SME ENTERPRISE PARTNER</span>
				</div>
				<h1 class="font-serif text-3xl sm:text-4xl font-normal text-[#0D1B3E] tracking-tight">
					Welcome, {user?.businessName || user?.name || 'SME Partner'}
				</h1>
				<p class="mt-1 text-sm text-[#465A7A] max-w-xl font-sans">
					Post high-priority tasks at zero cost. Corporates fund the youth wages, providing your business with operational leverage.
				</p>
			</div>

			<div class="flex items-center gap-3">
				<button
					type="button"
					class="cape-btn-primary px-5 py-3 text-xs font-mono uppercase tracking-widest shadow-xs flex items-center gap-2"
					onclick={() => (showPostForm = !showPostForm)}
				>
					<span>{showPostForm ? '✕ Close Form' : '+ Post New Task'}</span>
				</button>
			</div>
		</div>

		<!-- Error Banner -->
		<ApiError message={errorMessage} onretry={loadTasks} />

		<!-- Section: Post a Task Form (Sharp, Cape Cobalt) -->
		{#if showPostForm}
			<div class="border-2 border-[#1446A0] bg-white p-6 sm:p-8 mb-8 shadow-md">
				<div class="flex items-center justify-between pb-4 border-b border-[#C8D4E8] mb-6">
					<div>
						<h2 class="font-serif text-2xl text-[#0D1B3E]">Post a New SME Task (Free)</h2>
						<p class="text-xs text-[#6A84AC] font-sans">Funded by Corporate Enterprise Development reserves.</p>
					</div>
					<button
						type="button"
						class="text-[#6A84AC] hover:text-[#0D1B3E] p-1 font-mono text-sm"
						onclick={() => (showPostForm = false)}
						aria-label="Close form"
					>
						[ESC]
					</button>
				</div>

				<ApiError message={postError} ondismiss={() => (postError = null)} />

				<form onsubmit={handlePostTask} class="space-y-6">
					<div class="grid grid-cols-1 md:grid-cols-2 gap-6">
						<!-- Title -->
						<div class="md:col-span-2">
							<label for="taskTitle" class="block text-xs font-mono font-bold uppercase tracking-wider text-[#0D1B3E]">
								Task Title *
							</label>
							<input
								id="taskTitle"
								type="text"
								required
								maxlength="200"
								bind:value={taskTitle}
								placeholder="e.g. Social Media Design & Scheduling"
								class="cape-input mt-1.5 block w-full px-4 py-2.5 text-sm"
							/>
						</div>

						<!-- Category -->
						<div>
							<label for="taskCategory" class="block text-xs font-mono font-bold uppercase tracking-wider text-[#0D1B3E]">
								Category *
							</label>
							<select
								id="taskCategory"
								bind:value={taskCategory}
								class="cape-input mt-1.5 block w-full px-4 py-2.5 text-sm"
							>
								{#each categories as cat}
									<option value={cat}>{cat}</option>
								{/each}
							</select>
						</div>

						<!-- Budget (Corporate Funded) -->
						<div>
							<label for="taskBudget" class="block text-xs font-mono font-bold uppercase tracking-wider text-[#0D1B3E]">
								Youth Wage Budget (ZAR)
							</label>
							<div class="relative mt-1.5">
								<span class="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3.5 text-[#6A84AC] text-sm font-mono font-bold">
									R
								</span>
								<input
									id="taskBudget"
									type="number"
									step="50"
									min="0"
									bind:value={taskBudget}
									class="cape-input block w-full pl-8 pr-4 py-2.5 text-sm font-mono font-semibold"
								/>
							</div>
						</div>

						<!-- Duration Days -->
						<div>
							<label for="taskDuration" class="block text-xs font-mono font-bold uppercase tracking-wider text-[#0D1B3E]">
								Duration (Days)
							</label>
							<input
								id="taskDuration"
								type="number"
								min="1"
								max="90"
								bind:value={taskDuration}
								class="cape-input mt-1.5 block w-full px-4 py-2.5 text-sm font-mono"
							/>
						</div>

						<!-- Paid Flag -->
						<div class="flex items-center pt-6">
							<label class="flex items-center gap-3 cursor-pointer">
								<input
									type="checkbox"
									bind:checked={taskPaid}
									class="h-4 w-4 rounded-none text-[#1446A0] focus:ring-0 border-[#C8D4E8]"
								/>
								<span class="text-sm font-sans font-semibold text-[#0D1B3E]">
									Corporate Sponsored Stipend
								</span>
							</label>
						</div>

						<!-- Description -->
						<div class="md:col-span-2">
							<label for="taskDescription" class="block text-xs font-mono font-bold uppercase tracking-wider text-[#0D1B3E]">
								Detailed Deliverables &amp; Requirements
							</label>
							<textarea
								id="taskDescription"
								rows="3"
								maxlength="1000"
								bind:value={taskDescription}
								placeholder="Describe the objectives, deliverables, and expectations..."
								class="cape-input mt-1.5 block w-full px-4 py-2.5 text-sm"
							></textarea>
						</div>
					</div>

					<div class="flex items-center justify-end gap-3 pt-4 border-t border-[#C8D4E8]">
						<button
							type="button"
							class="cape-btn-secondary px-4 py-2.5 text-xs font-mono uppercase tracking-wider"
							onclick={() => (showPostForm = false)}
						>
							Cancel
						</button>
						<button
							type="submit"
							disabled={posting}
							class="cape-btn-primary px-6 py-2.5 text-xs font-mono uppercase tracking-widest disabled:opacity-50"
						>
							{posting ? 'Publishing...' : 'Commit Task to Hub'}
						</button>
					</div>
				</form>
			</div>
		{/if}

		<!-- Section: My Tasks Management -->
		<div class="space-y-4">
			<div class="flex items-center justify-between border-b border-[#C8D4E8] pb-3">
				<div>
					<h2 class="font-serif text-2xl text-[#0D1B3E]">My Posted Tasks</h2>
					<p class="text-xs text-[#6A84AC] font-sans">Track assignments, progress, and verify completed work records.</p>
				</div>
				<span class="font-mono text-xs font-bold text-[#1446A0] bg-[#EFF4FF] border border-[#C8D4E8] px-2.5 py-0.5">
					{tasks.length} TOTAL
				</span>
			</div>

			{#if loading}
				<div class="space-y-3">
					{#each Array(3) as _}
						<div class="h-28 border border-[#C8D4E8] bg-white animate-pulse"></div>
					{/each}
				</div>
			{:else if tasks.length === 0}
				<div class="border border-dashed border-[#C8D4E8] bg-white p-12 text-center">
					<h3 class="font-serif text-lg text-[#0D1B3E]">No tasks created yet</h3>
					<p class="mt-1 text-xs text-[#6A84AC] max-w-sm mx-auto">
						Start delegating work to skilled youth program alumni.
					</p>
					<div class="mt-4">
						<button
							type="button"
							class="cape-btn-primary px-4 py-2 text-xs font-mono uppercase tracking-wider"
							onclick={() => (showPostForm = true)}
						>
							Post Task Now
						</button>
					</div>
				</div>
			{:else}
				<div class="space-y-4">
					{#each tasks as task (task.id)}
						<div class="border border-[#C8D4E8] bg-white p-6 shadow-xs flex flex-col md:flex-row md:items-center md:justify-between gap-4 hover:border-[#1446A0] transition-colors">
							<div class="space-y-2 flex-1">
								<div class="flex items-center gap-3 flex-wrap">
									<StatusBadge status={task.status} />
									<span class="bg-[#EFF4FF] border border-[#C8D4E8] px-2 py-0.5 text-xs font-mono text-[#1446A0]">
										{task.category}
									</span>
									<span class="text-xs font-mono text-[#6A84AC]">
										Created {formatDate(task.createdAt)}
									</span>
								</div>

								<h3 class="font-serif text-xl text-[#0D1B3E]">
									<a href={`/tasks/${task.id}`} class="hover:text-[#1446A0] hover:underline">
										{task.title}
									</a>
								</h3>

								{#if task.description}
									<p class="text-xs text-[#465A7A] line-clamp-1 max-w-2xl font-sans">{task.description}</p>
								{/if}

								<div class="flex items-center gap-4 text-xs font-mono text-[#6A84AC] pt-1">
									<span class="font-bold text-[#1446A0]">{formatCurrency(task.budget)}</span>
									<span>•</span>
									<span>{formatDuration(task.durationDays)}</span>
									{#if task.assignedToName}
										<span>•</span>
										<span class="text-[#0D1B3E] font-semibold">Assigned: {task.assignedToName}</span>
									{/if}
								</div>
							</div>

							<!-- Action Buttons -->
							<div class="flex items-center gap-2.5 shrink-0 pt-3 md:pt-0 border-t md:border-t-0 border-[#C8D4E8]">
								{#if task.status === 'OPEN'}
									<button
										type="button"
										class="border border-[#1446A0] bg-[#EFF4FF] px-4 py-2 text-xs font-mono uppercase tracking-wider font-bold text-[#1446A0] hover:bg-[#1446A0] hover:text-white transition-colors"
										onclick={() => openAssignModal(task)}
									>
										Assign to Youth
									</button>
								{:else if task.status === 'IN_PROGRESS' || task.status === 'COMPLETED'}
									<button
										type="button"
										class="bg-[#7C3FE4] px-4 py-2 text-xs font-mono uppercase tracking-wider font-bold text-white shadow-xs hover:bg-[#682ec7] transition-colors"
										onclick={() => openVerifyModal(task)}
									>
										Verify Experience
									</button>
								{:else if task.status === 'VERIFIED'}
									<span class="border border-[#7C3FE4] bg-[#F5EEFF] px-3 py-1.5 text-xs font-mono font-bold text-[#7C3FE4] uppercase">
										✓ Verified in Ledger
									</span>
								{/if}

								<a
									href={`/tasks/${task.id}`}
									class="border border-[#C8D4E8] px-3.5 py-2 text-xs font-mono uppercase tracking-wider text-[#0D1B3E] hover:bg-[#F8F9FC] transition-colors"
								>
									Details
								</a>
							</div>
						</div>
					{/each}
				</div>
			{/if}
		</div>
	</div>
</RoleGuard>

<!-- Modal: Assign Task to Youth (Sharp) -->
<Modal
	bind:open={assignModalOpen}
	title={`Assign Task: ${selectedTaskToAssign?.title || ''}`}
>
	<div class="space-y-4">
		<p class="text-xs text-[#465A7A] leading-relaxed font-sans">
			Specify the Youth Candidate ID to assign this task. Once assigned, the task status advances to
			<span class="font-mono font-bold text-[#D4831A]">IN_PROGRESS</span>.
		</p>

		<div>
			<label for="youthIdInput" class="block text-xs font-mono font-bold uppercase tracking-wider text-[#0D1B3E]">
				Candidate ID *
			</label>
			<input
				id="youthIdInput"
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
			disabled={assigning}
			class="cape-btn-primary px-5 py-2 text-xs font-mono uppercase tracking-widest disabled:opacity-50"
			onclick={handleAssignSubmit}
		>
			{assigning ? 'Assigning...' : 'Confirm Assignment'}
		</button>
	{/snippet}
</Modal>

<!-- Modal: Verify Experience (Sharp) -->
<Modal
	bind:open={verifyModalOpen}
	title={`Verify Work: ${selectedTaskToVerify?.title || ''}`}
>
	<div class="space-y-4">
		<p class="text-xs text-[#465A7A] leading-relaxed font-sans">
			Your evaluation unlocks the candidate's guaranteed corporate wage disbursement and adds permanent proof to their Experience Ledger.
		</p>

		<div>
			<div class="block text-xs font-mono font-bold uppercase tracking-wider text-[#0D1B3E] mb-1.5">
				Performance Rating (0 to 5 Stars) *
			</div>
			<StarRating bind:rating={verifyRating} interactive={true} size="lg" />
		</div>

		<div>
			<label for="verifyFeedbackInput" class="block text-xs font-mono font-bold uppercase tracking-wider text-[#0D1B3E]">
				Written Recommendation &amp; Feedback *
			</label>
			<textarea
				id="verifyFeedbackInput"
				rows="3"
				bind:value={verifyFeedback}
				placeholder="Provide specific comments on quality, speed, and deliverables..."
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
			disabled={verifying}
			class="bg-[#7C3FE4] text-white px-5 py-2 text-xs font-mono uppercase tracking-widest font-bold hover:bg-[#682ec7] disabled:opacity-50"
			onclick={handleVerifySubmit}
		>
			{verifying ? 'Verifying...' : 'Sign & Record to Ledger'}
		</button>
	{/snippet}
</Modal>
